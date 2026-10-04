package com.example.domain.model

sealed interface DecodedInstruction {
    val programId: String

    data class TransferChecked(
        override val programId: String,
        val mint: String,
        val source: String,
        val dest: String,
        val authority: String,
        val rawAmount: Long,
        val decimals: Int
    ) : DecodedInstruction

    data class SplTransfer(
        override val programId: String,
        val source: String,
        val dest: String,
        val authority: String,
        val rawAmount: Long
    ) : DecodedInstruction

    data class SystemTransfer(
        override val programId: String,
        val from: String,
        val to: String,
        val lamports: Long
    ) : DecodedInstruction

    data class Memo(
        override val programId: String,
        val memoText: String
    ) : DecodedInstruction

    data class Unsupported(
        override val programId: String,
        val reason: String
    ) : DecodedInstruction
}

data class ReviewedAction(
    val messageHash: String,
    val network: String,
    val account: String,
    val reviewedAt: Long = System.currentTimeMillis()
)
