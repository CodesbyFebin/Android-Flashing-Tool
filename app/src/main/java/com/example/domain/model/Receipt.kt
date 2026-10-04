package com.example.domain.model

import java.util.UUID

enum class SettlementStatus(val displayName: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    FINALIZED("Finalized"),
    FAILED("Failed")
}

data class Receipt(
    val id: String = UUID.randomUUID().toString(),
    val actionId: String,
    val taskId: String = "",
    val title: String = "",
    val evidenceDigest: String, // manifestSha256
    val transactionSignature: String,
    val settlementStatus: SettlementStatus = SettlementStatus.CONFIRMED,
    val observedAt: Long = System.currentTimeMillis(),
    val blockSlot: Long = 318492041L,
    val feeLamports: Long = 5000L,
    val payer: String = "",
    val recipient: String = "",
    val amountFormatted: String = "",
    val tokenSymbol: String = "SKR",
    val network: String = "devnet",
    val memo: String = ""
) {
    val explorerUrl: String
        get() = if (network == "mainnet-beta") {
            "https://solscan.io/tx/$transactionSignature"
        } else {
            "https://solscan.io/tx/$transactionSignature?cluster=devnet"
        }
}
