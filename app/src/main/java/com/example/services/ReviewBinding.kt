package com.example.services

import com.example.domain.model.Action
import com.example.domain.model.Evidence
import com.example.domain.model.ReviewedAction
import com.example.security.SHA256Service

object ReviewBinding {

    /**
     * Constructs the deterministic canonical transaction message payload
     * binding the action parameters, recipient, amount, network, and DePIN evidence digest.
     */
    fun serializeMessagePayload(
        action: Action,
        evidenceDigest: String? = null,
        userAccount: String
    ): ByteArray {
        val canonical = buildString {
            append("SOLANA_CLEARANCE_MSG_V1\n")
            append("NETWORK:${action.network}\n")
            append("ACCOUNT:$userAccount\n")
            append("RECIPIENT:${action.recipient}\n")
            append("MINT:${action.mint}\n")
            append("RAW_AMOUNT:${action.rawAmount}\n")
            append("DECIMALS:${action.decimals}\n")
            append("TASK_ID:${action.taskId}\n")
            if (!evidenceDigest.isNullOrBlank()) {
                append("EVIDENCE_DIGEST:$evidenceDigest\n")
            }
        }
        return canonical.toByteArray(Charsets.UTF_8)
    }

    fun prepareReview(
        action: Action,
        evidence: Evidence?,
        userAccount: String
    ): ReviewedAction {
        val serialized = serializeMessagePayload(action, evidence?.manifestSha256, userAccount)
        val messageHash = SHA256Service.hash(serialized)
        return ReviewedAction(
            messageHash = messageHash,
            network = action.network,
            account = userAccount,
            reviewedAt = System.currentTimeMillis()
        )
    }

    /**
     * Ensures transaction message has not been altered since the user reviewed it.
     * Throws IllegalStateException("MESSAGE_CHANGED") on tamper detection.
     */
    fun assertUnchanged(
        currentSerialized: ByteArray,
        review: ReviewedAction
    ) {
        val currentHash = SHA256Service.hash(currentSerialized)
        if (!currentHash.equals(review.messageHash, ignoreCase = true)) {
            throw IllegalStateException("MESSAGE_CHANGED: Transaction payload altered after user review! Expected [${review.messageHash.take(12)}...], but got [${currentHash.take(12)}...]. Authorization aborted for user safety.")
        }
    }
}
