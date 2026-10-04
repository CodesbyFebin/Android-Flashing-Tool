package com.example.domain.model

import java.util.UUID

enum class ActionStatus(val displayName: String) {
    DRAFT("Draft"),
    REVIEWED("Reviewed"),
    AUTHORIZED("Authorized"),
    SETTLED("Settled"),
    REJECTED("Rejected")
}

data class Action(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val description: String,
    val category: String, // e.g., "DePIN Verification", "Token Payment", "Bandwidth Relay", "Sensor Survey"
    val recipient: String,
    val mint: String, // e.g. SKR mint address or Native SOL
    val rawAmount: String, // Decimal string, e.g. "1000000000"
    val decimals: Int = 9,
    val network: String = "devnet", // "devnet" | "mainnet-beta"
    val status: ActionStatus = ActionStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedAmount: String
        get() {
            return try {
                val value = rawAmount.toBigDecimal().movePointLeft(decimals)
                value.stripTrailingZeros().toPlainString()
            } catch (e: Exception) {
                rawAmount
            }
        }

    val tokenSymbol: String
        get() = when {
            mint.startsWith("SKR") || mint.contains("skr", ignoreCase = true) -> "SKR"
            mint == "11111111111111111111111111111111" || mint.equals("SOL", ignoreCase = true) -> "SOL"
            mint.contains("USDC", ignoreCase = true) -> "USDC"
            else -> "SPL"
        }
}
