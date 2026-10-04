package com.example

import com.example.domain.model.Action
import com.example.domain.model.DecodedInstruction
import com.example.domain.model.Evidence
import com.example.security.SHA256Service
import com.example.services.ReviewBinding
import com.example.services.SolanaDecoder
import com.example.services.TransactionExplainer
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCanonicalManifestHashing() {
        val actionId = "TEST-TASK-001"
        val photoSha = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val note = "Site verification complete"
        val capturedAt = 1700000000000L

        val (canonicalJson, hash) = SHA256Service.buildAndHashCanonicalManifest(
            actionId, photoSha, note, capturedAt
        )

        assertTrue(canonicalJson.startsWith("[[\"actionId\",\"TEST-TASK-001\"]"))
        assertEquals(64, hash.length)

        val isValid = SHA256Service.verifyIntegrity(actionId, photoSha, note, capturedAt, hash)
        assertTrue(isValid)

        val isInvalid = SHA256Service.verifyIntegrity(actionId, photoSha, "Tampered Note", capturedAt, hash)
        assertFalse(isInvalid)
    }

    @Test
    fun testMultiEvidenceManifestHashing() {
        val actionId = "HE-5G-8821"
        val photoSha = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val videoSha = "7a4b89e1f5d23c890124ba56d47e8912c45e78a90123fe4567890123456789ab"
        val docSha = "8f3e21a094b81c7e9014b2d56c87a912e34f67b89012cd34567890123456789c"
        val note = "Full multi-sensor bundle verified"
        val capturedAt = 1700000000000L

        val (canonicalJson, hash) = SHA256Service.buildAndHashCanonicalManifest(
            actionId = actionId,
            photoSha256 = photoSha,
            note = note,
            capturedAt = capturedAt,
            videoSha256 = videoSha,
            documentSha256 = docSha
        )

        assertTrue(canonicalJson.contains("videoSha256"))
        assertTrue(canonicalJson.contains("documentSha256"))
        assertEquals(64, hash.length)

        val isValid = SHA256Service.verifyIntegrity(
            actionId = actionId,
            photoSha256 = photoSha,
            note = note,
            capturedAt = capturedAt,
            expectedManifestSha256 = hash,
            videoSha256 = videoSha,
            documentSha256 = docSha
        )
        assertTrue(isValid)
    }

    @Test
    fun testSolanaTransferCheckedDecoder() {
        val payload = SolanaDecoder.buildMockTransferCheckedPayload(
            rawAmount = 2500000000L,
            decimals = 9
        )
        val keys = listOf("source_account", "mint_skr", "dest_account", "authority_key")

        val decoded = SolanaDecoder.decodeInstruction(
            programId = SolanaDecoder.TOKEN_PROGRAM_ID,
            keys = keys,
            data = payload
        )

        assertTrue(decoded is DecodedInstruction.TransferChecked)
        val transferChecked = decoded as DecodedInstruction.TransferChecked
        assertEquals(2500000000L, transferChecked.rawAmount)
        assertEquals(9, transferChecked.decimals)
        assertEquals("mint_skr", transferChecked.mint)
        assertEquals("dest_account", transferChecked.dest)
    }

    @Test
    fun testTransactionExplainer() {
        val action = Action(
            taskId = "HE-5G-8821",
            title = "Helium Mobile Payout",
            description = "Radio beacon survey",
            category = "DePIN Work",
            recipient = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
            mint = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84",
            rawAmount = "2500000000"
        )
        val explanation = TransactionExplainer.explainAction(
            action = action,
            evidence = null,
            senderAccount = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU"
        )

        assertTrue(explanation.isSafe)
        assertTrue(explanation.headline.contains("2.5"))
        assertTrue(explanation.headline.contains("SKR"))
        assertTrue(explanation.balanceImpactSummary.contains("-2.5"))
        assertEquals("0.000005 SOL (5,000 Lamports)", explanation.maxNetworkFeeSol)
    }

    @Test
    fun testReviewBindingTamperDetection() {
        val action = Action(
            id = "act-1",
            taskId = "TASK-001",
            title = "Test Payout",
            description = "DePIN Bounty",
            category = "DePIN Work",
            recipient = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
            mint = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84",
            rawAmount = "1000000000"
        )
        val userAccount = "MySignerWallet111111111111111111111111111"
        val review = ReviewBinding.prepareReview(action, null, userAccount)

        val validPayload = ReviewBinding.serializeMessagePayload(action, null, userAccount)
        // Should succeed without throwing
        ReviewBinding.assertUnchanged(validPayload, review)

        // Simulate tampering (tampered recipient)
        val tamperedAction = action.copy(recipient = "DRAINER_MALICIOUS_PUBKEY_999999999999999999")
        val tamperedPayload = ReviewBinding.serializeMessagePayload(tamperedAction, null, userAccount)

        try {
            ReviewBinding.assertUnchanged(tamperedPayload, review)
            fail("Expected MESSAGE_CHANGED exception upon tampered payload")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("MESSAGE_CHANGED") == true)
        }
    }
}
