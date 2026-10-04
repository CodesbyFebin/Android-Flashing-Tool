package com.example.data

import com.example.ai.GeminiAuditService
import com.example.ai.SecurityAuditResult
import com.example.domain.model.Action
import com.example.domain.model.ActionStatus
import com.example.domain.model.DePinTask
import com.example.domain.model.DePinTaskStatus
import com.example.domain.model.Evidence
import com.example.domain.model.Receipt
import com.example.domain.model.ReviewedAction
import com.example.domain.model.SettlementStatus
import com.example.security.KeystoreService
import com.example.security.SHA256Service
import com.example.services.ConnectedWallet
import com.example.services.MwaWalletService
import com.example.services.ReviewBinding
import com.example.services.SolanaRpcService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ClearanceRepository(
    private val dao: ClearanceDao,
    val walletService: MwaWalletService = MwaWalletService(),
    val rpcService: SolanaRpcService = SolanaRpcService(),
    val keystoreService: KeystoreService = KeystoreService(),
    val aiAuditService: GeminiAuditService = GeminiAuditService()
) {

    val actions: Flow<List<Action>> = dao.getAllActions().map { list ->
        list.map { it.toDomain() }
    }

    val receipts: Flow<List<Receipt>> = dao.getAllReceipts().map { list ->
        list.map { it.toDomain() }
    }

    val dePinTasks: Flow<List<DePinTask>> = dao.getAllDePinTasks().map { list ->
        list.map { it.toDomain() }
    }

    val walletState: Flow<ConnectedWallet?> = walletService.walletState

    suspend fun getActionById(id: String): Action? {
        return dao.getActionById(id)?.toDomain()
    }

    suspend fun getEvidenceForAction(actionId: String): Evidence? {
        return dao.getEvidenceForAction(actionId)?.toDomain()
    }

    suspend fun getReceiptByActionId(actionId: String): Receipt? {
        return dao.getReceiptByActionId(actionId)?.toDomain()
    }

    suspend fun getReceiptById(receiptId: String): Receipt? {
        return dao.getReceiptById(receiptId)?.toDomain()
    }

    suspend fun getDePinTaskById(id: String): DePinTask? {
        return dao.getDePinTaskById(id)?.toDomain()
    }

    suspend fun saveAction(action: Action) {
        dao.insertAction(ActionEntity.fromDomain(action))
    }

    suspend fun saveEvidence(evidence: Evidence) {
        dao.insertEvidence(EvidenceEntity.fromDomain(evidence))
    }

    suspend fun saveDePinTask(task: DePinTask) {
        dao.insertDePinTask(DePinTaskEntity.fromDomain(task))
    }

    suspend fun markDePinTaskComplete(taskId: String) {
        dao.updateDePinTaskCompletion(taskId, DePinTaskStatus.PROOF_SUBMITTED.name, System.currentTimeMillis())
    }

    suspend fun triggerDePinTaskPayment(task: DePinTask, recipientAccount: String): Action {
        val actionId = "act_${UUID.randomUUID().toString().take(8)}"
        val rawAmt = try {
            val bd = task.rewardAmount.toBigDecimal().movePointRight(9)
            bd.toBigInteger().toString()
        } catch (e: Exception) {
            "1000000000"
        }

        val action = Action(
            id = actionId,
            taskId = task.id,
            title = "Payout: ${task.title}",
            description = "Authorization for completed DePIN task on ${task.network} (Node: ${task.deviceOrNodeId})",
            category = task.category,
            recipient = recipientAccount,
            mint = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84",
            rawAmount = rawAmt,
            decimals = 9,
            network = "devnet",
            status = ActionStatus.DRAFT,
            createdAt = System.currentTimeMillis()
        )
        dao.insertAction(ActionEntity.fromDomain(action))
        dao.linkDePinTaskAction(task.id, actionId, DePinTaskStatus.PAYMENT_PENDING.name)
        return action
    }

    suspend fun updateActionStatus(actionId: String, status: ActionStatus) {
        dao.updateActionStatus(actionId, status.name)
    }

    suspend fun deleteAction(actionId: String) {
        dao.deleteAction(actionId)
    }

    fun prepareReviewBinding(action: Action, evidence: Evidence?, userAccount: String): ReviewedAction {
        return ReviewBinding.prepareReview(action, evidence, userAccount)
    }

    suspend fun performSecurityAudit(action: Action, evidence: Evidence?): SecurityAuditResult {
        return aiAuditService.auditTransaction(action, evidence)
    }

    /**
     * Executes the authoritative clearance workflow:
     * 1. Assert review binding unchanged (tamper detection)
     * 2. Sign via MWA / Seeker Seed Vault
     * 3. Observe on-chain settlement
     * 4. Record verifiable receipt
     */
    suspend fun authorizeAndSettle(
        action: Action,
        evidence: Evidence?,
        userAccount: String,
        review: ReviewedAction,
        simulateTampering: Boolean = false
    ): Receipt {
        // Step 1: Validate Review Binding (Fail fast if message was mutated)
        val payload = if (simulateTampering) {
            // Negative test: Inject a drainer/tampered recipient
            val tamperedAction = action.copy(recipient = "DRAINER_ATTACK_UNAUTHORIZED_RECIPIENT_3341")
            ReviewBinding.serializeMessagePayload(tamperedAction, evidence?.manifestSha256, userAccount)
        } else {
            ReviewBinding.serializeMessagePayload(action, evidence?.manifestSha256, userAccount)
        }

        // Will throw IllegalStateException("MESSAGE_CHANGED") if tampered
        ReviewBinding.assertUnchanged(payload, review)

        // Step 2: Update status to AUTHORIZED
        dao.updateActionStatus(action.id, ActionStatus.AUTHORIZED.name)

        // Step 3: Sign with MWA / Seeker Seed Vault
        val signature = walletService.signTransaction(payload)

        // Step 4: Query live network slot and observe settlement
        val slot = rpcService.getSlot()
        val settlementStatus = rpcService.observeSettlement(signature)

        // Step 5: Construct permanent verifiable receipt
        val receipt = Receipt(
            actionId = action.id,
            taskId = action.taskId,
            title = action.title,
            evidenceDigest = evidence?.manifestSha256 ?: SHA256Service.hashString(action.id),
            transactionSignature = signature,
            settlementStatus = settlementStatus,
            observedAt = System.currentTimeMillis(),
            blockSlot = slot,
            feeLamports = 5000L,
            payer = userAccount,
            recipient = action.recipient,
            amountFormatted = action.formattedAmount,
            tokenSymbol = action.tokenSymbol,
            network = action.network,
            memo = "CLEARANCE:EVIDENCE_HASH:${evidence?.manifestSha256 ?: "NONE"}"
        )

        dao.insertReceipt(ReceiptEntity.fromDomain(receipt))
        dao.updateActionStatus(action.id, ActionStatus.SETTLED.name)
        dao.linkDePinTaskReceiptByActionId(action.id, receipt.id, DePinTaskStatus.COMPLETED_SETTLED.name)

        return receipt
    }

    suspend fun rejectAction(actionId: String) {
        dao.updateActionStatus(actionId, ActionStatus.REJECTED.name)
    }

    suspend fun fetchSolBalance(address: String): Long {
        return rpcService.getBalance(address)
    }

    suspend fun requestDevnetAirdrop(address: String): String {
        return rpcService.requestAirdrop(address)
    }
}
