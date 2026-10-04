package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Action
import com.example.domain.model.ActionStatus
import com.example.domain.model.DePinTask
import com.example.domain.model.DePinTaskStatus
import com.example.domain.model.Evidence
import com.example.domain.model.Receipt
import com.example.domain.model.SettlementStatus

@Entity(tableName = "actions")
data class ActionEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val title: String,
    val description: String,
    val category: String,
    val recipient: String,
    val mint: String,
    val rawAmount: String,
    val decimals: Int,
    val network: String,
    val status: String,
    val createdAt: Long
) {
    fun toDomain(): Action {
        return Action(
            id = id,
            taskId = taskId,
            title = title,
            description = description,
            category = category,
            recipient = recipient,
            mint = mint,
            rawAmount = rawAmount,
            decimals = decimals,
            network = network,
            status = try { ActionStatus.valueOf(status) } catch (e: Exception) { ActionStatus.DRAFT },
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(a: Action) = ActionEntity(
            id = a.id,
            taskId = a.taskId,
            title = a.title,
            description = a.description,
            category = a.category,
            recipient = a.recipient,
            mint = a.mint,
            rawAmount = a.rawAmount,
            decimals = a.decimals,
            network = a.network,
            status = a.status.name,
            createdAt = a.createdAt
        )
    }
}

@Entity(tableName = "evidence")
data class EvidenceEntity(
    @PrimaryKey val actionId: String,
    val photoPath: String,
    val photoSha256: String,
    val videoPath: String? = null,
    val videoSha256: String? = null,
    val documentPath: String? = null,
    val documentSha256: String? = null,
    val documentName: String? = null,
    val note: String,
    val capturedAt: Long,
    val manifestSha256: String,
    val deviceAttestationSignature: String
) {
    fun toDomain(): Evidence {
        return Evidence(
            actionId = actionId,
            photoPath = photoPath,
            photoSha256 = photoSha256,
            videoPath = videoPath,
            videoSha256 = videoSha256,
            documentPath = documentPath,
            documentSha256 = documentSha256,
            documentName = documentName,
            note = note,
            capturedAt = capturedAt,
            manifestSha256 = manifestSha256,
            deviceAttestationSignature = deviceAttestationSignature
        )
    }

    companion object {
        fun fromDomain(e: Evidence) = EvidenceEntity(
            actionId = e.actionId,
            photoPath = e.photoPath,
            photoSha256 = e.photoSha256,
            videoPath = e.videoPath,
            videoSha256 = e.videoSha256,
            documentPath = e.documentPath,
            documentSha256 = e.documentSha256,
            documentName = e.documentName,
            note = e.note,
            capturedAt = e.capturedAt,
            manifestSha256 = e.manifestSha256,
            deviceAttestationSignature = e.deviceAttestationSignature
        )
    }
}

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val actionId: String,
    val taskId: String,
    val title: String,
    val evidenceDigest: String,
    val transactionSignature: String,
    val settlementStatus: String,
    val observedAt: Long,
    val blockSlot: Long,
    val feeLamports: Long,
    val payer: String,
    val recipient: String,
    val amountFormatted: String,
    val tokenSymbol: String,
    val network: String,
    val memo: String
) {
    fun toDomain(): Receipt {
        return Receipt(
            id = id,
            actionId = actionId,
            taskId = taskId,
            title = title,
            evidenceDigest = evidenceDigest,
            transactionSignature = transactionSignature,
            settlementStatus = try { SettlementStatus.valueOf(settlementStatus) } catch (e: Exception) { SettlementStatus.CONFIRMED },
            observedAt = observedAt,
            blockSlot = blockSlot,
            feeLamports = feeLamports,
            payer = payer,
            recipient = recipient,
            amountFormatted = amountFormatted,
            tokenSymbol = tokenSymbol,
            network = network,
            memo = memo
        )
    }

    companion object {
        fun fromDomain(r: Receipt) = ReceiptEntity(
            id = r.id,
            actionId = r.actionId,
            taskId = r.taskId,
            title = r.title,
            evidenceDigest = r.evidenceDigest,
            transactionSignature = r.transactionSignature,
            settlementStatus = r.settlementStatus.name,
            observedAt = r.observedAt,
            blockSlot = r.blockSlot,
            feeLamports = r.feeLamports,
            payer = r.payer,
            recipient = r.recipient,
            amountFormatted = r.amountFormatted,
            tokenSymbol = r.tokenSymbol,
            network = r.network,
            memo = r.memo
        )
    }
}

@Entity(tableName = "depin_tasks")
data class DePinTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val network: String,
    val category: String,
    val description: String,
    val rewardAmount: String,
    val rewardToken: String,
    val deviceOrNodeId: String,
    val status: String,
    val assignedAt: Long,
    val completedAt: Long?,
    val actionId: String?,
    val receiptId: String?,
    val requirements: String,
    val attachedFilesCount: Int
) {
    fun toDomain(): DePinTask {
        return DePinTask(
            id = id,
            title = title,
            network = network,
            category = category,
            description = description,
            rewardAmount = rewardAmount,
            rewardToken = rewardToken,
            deviceOrNodeId = deviceOrNodeId,
            status = try { DePinTaskStatus.valueOf(status) } catch (e: Exception) { DePinTaskStatus.ASSIGNED },
            assignedAt = assignedAt,
            completedAt = completedAt,
            actionId = actionId,
            receiptId = receiptId,
            requirements = requirements,
            attachedFilesCount = attachedFilesCount
        )
    }

    companion object {
        fun fromDomain(t: DePinTask) = DePinTaskEntity(
            id = t.id,
            title = t.title,
            network = t.network,
            category = t.category,
            description = t.description,
            rewardAmount = t.rewardAmount,
            rewardToken = t.rewardToken,
            deviceOrNodeId = t.deviceOrNodeId,
            status = t.status.name,
            assignedAt = t.assignedAt,
            completedAt = t.completedAt,
            actionId = t.actionId,
            receiptId = t.receiptId,
            requirements = t.requirements,
            attachedFilesCount = t.attachedFilesCount
        )
    }
}
