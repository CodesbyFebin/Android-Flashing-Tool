package com.example.domain.model

enum class ClaimStatus(val displayName: String) {
    CLAIMABLE_NOW("Claimable Now"),
    CLAIMED("Claimed"),
    EXPIRED("Expired")
}

data class SeasonClaim(
    val seasonId: String,
    val seasonName: String,
    val allocatedSkr: String,
    val claimDeadlineTimestamp: Long, // 90 days window
    val requiredSolFloor: Double = 0.015,
    val status: ClaimStatus = ClaimStatus.CLAIMABLE_NOW,
    val questProofDigest: String = ""
) {
    val daysRemaining: Int
        get() {
            val diff = claimDeadlineTimestamp - System.currentTimeMillis()
            return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0).toInt()
        }

    val isUrgent: Boolean
        get() = daysRemaining <= 10 && status == ClaimStatus.CLAIMABLE_NOW
}

data class SkrStakePosition(
    val guardianName: String,
    val guardianPubkey: String,
    val stakedAmount: String, // e.g. "30.00"
    val coolingDownAmount: String, // e.g. "12.50"
    val liquidAmount: String, // e.g. "6.00"
    val apyPercent: Double = 26.2,
    val cooldownEndTimestamp: Long? = null,
    val claimableEpochRewards: String = "0.48",
    val epoch: Int = 684
) {
    val totalBalanceFormatted: String
        get() {
            val s = stakedAmount.toDoubleOrNull() ?: 0.0
            val c = coolingDownAmount.toDoubleOrNull() ?: 0.0
            val l = liquidAmount.toDoubleOrNull() ?: 0.0
            return "%.2f".format(s + c + l)
        }

    val hoursRemainingInCooldown: Int
        get() {
            if (cooldownEndTimestamp == null) return 0
            val diff = cooldownEndTimestamp - System.currentTimeMillis()
            return (diff / (1000 * 60 * 60)).coerceAtLeast(0).toInt()
        }

    val isCooldownActive: Boolean
        get() = (coolingDownAmount.toDoubleOrNull() ?: 0.0) > 0.0 && hoursRemainingInCooldown > 0

    val isWithdrawReady: Boolean
        get() = (coolingDownAmount.toDoubleOrNull() ?: 0.0) > 0.0 && hoursRemainingInCooldown == 0
}
