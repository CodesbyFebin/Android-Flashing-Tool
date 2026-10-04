package com.example.services

import com.example.domain.model.ClaimStatus
import com.example.domain.model.SeasonClaim
import com.example.domain.model.SkrStakePosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GuardianInfo(
    val name: String,
    val pubkey: String,
    val apy: Double,
    val uptimePercent: Double,
    val totalStakedSkr: String,
    val verifiedAppSubmissionsCurated: Int
)

class SkrStakingService {

    companion object {
        const val OFFICIAL_SKR_MINT = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84"
        const val MINIMUM_SOL_FEE_FLOOR = 0.015
        const val COOLDOWN_DURATION_HOURS = 48
    }

    val guardians = listOf(
        GuardianInfo(
            name = "Helium Mobile Guardian",
            pubkey = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
            apy = 26.5,
            uptimePercent = 99.98,
            totalStakedSkr = "1,420,000 SKR",
            verifiedAppSubmissionsCurated = 142
        ),
        GuardianInfo(
            name = "EthelSec Sentinel Guardian",
            pubkey = "4Nd1mBQtrMKBCb2u3hGzV8jN4e6V2nK8mP2qR4sT6uV8",
            apy = 26.1,
            uptimePercent = 100.0,
            totalStakedSkr = "2,150,000 SKR",
            verifiedAppSubmissionsCurated = 289
        ),
        GuardianInfo(
            name = "Seeker Genesis Community Node",
            pubkey = "3KmB7uV8jN4e6V2nK8mP2qR4sT6uV8Nd1mBQtrMKBCb2",
            apy = 25.8,
            uptimePercent = 99.95,
            totalStakedSkr = "890,000 SKR",
            verifiedAppSubmissionsCurated = 94
        )
    )

    private val now = System.currentTimeMillis()
    // Simulated active cooldown finishing in 19 hours
    private val initialCooldownEnd = now + (19 * 60 * 60 * 1000)

    private val _stakePosition = MutableStateFlow(
        SkrStakePosition(
            guardianName = "EthelSec Sentinel Guardian",
            guardianPubkey = "4Nd1mBQtrMKBCb2u3hGzV8jN4e6V2nK8mP2qR4sT6uV8",
            stakedAmount = "30.00",
            coolingDownAmount = "12.50",
            liquidAmount = "6.00",
            apyPercent = 26.2,
            cooldownEndTimestamp = initialCooldownEnd,
            claimableEpochRewards = "0.78",
            epoch = 684
        )
    )
    val stakePosition: StateFlow<SkrStakePosition> = _stakePosition.asStateFlow()

    private val _seasonClaims = MutableStateFlow(
        listOf(
            SeasonClaim(
                seasonId = "SEASON_01_GENESIS",
                seasonName = "Seeker Genesis Season 1 Airdrop",
                allocatedSkr = "25.00",
                claimDeadlineTimestamp = now + (42L * 24 * 60 * 60 * 1000), // 42 days remaining of 90
                requiredSolFloor = 0.015,
                status = ClaimStatus.CLAIMABLE_NOW,
                questProofDigest = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"
            ),
            SeasonClaim(
                seasonId = "QUEST_SUMMER_ROUND_2",
                seasonName = "Seeker Summer Round 2 Quest Reward",
                allocatedSkr = "15.00",
                claimDeadlineTimestamp = now + (8L * 24 * 60 * 60 * 1000), // 8 days remaining (Urgent!)
                requiredSolFloor = 0.015,
                status = ClaimStatus.CLAIMABLE_NOW,
                questProofDigest = "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35"
            ),
            SeasonClaim(
                seasonId = "DEVNET_EARLY_BIRD",
                seasonName = "Genesis Builder Early Bird",
                allocatedSkr = "10.00",
                claimDeadlineTimestamp = now - (5L * 24 * 60 * 60 * 1000), // Expired
                requiredSolFloor = 0.015,
                status = ClaimStatus.EXPIRED,
                questProofDigest = "3a9f02b1c8e76451293adbc5e8f41039478129dcae6189b45012fe849102cba3"
            )
        )
    )
    val seasonClaims: StateFlow<List<SeasonClaim>> = _seasonClaims.asStateFlow()

    fun stageUnstake(amountToUnstake: Double, isFullUnstake: Boolean): Pair<Boolean, String> {
        val current = _stakePosition.value
        val staked = current.stakedAmount.toDoubleOrNull() ?: 0.0

        if (amountToUnstake <= 0.0 || amountToUnstake > staked) {
            return Pair(false, "Invalid amount: You only have $staked SKR staked.")
        }

        val newStaked = "%.2f".format(staked - amountToUnstake)
        val curCooling = current.coolingDownAmount.toDoubleOrNull() ?: 0.0
        val newCooling = "%.2f".format(curCooling + amountToUnstake)
        val cooldownEnd = System.currentTimeMillis() + (COOLDOWN_DURATION_HOURS * 60 * 60 * 1000L)

        _stakePosition.value = current.copy(
            stakedAmount = newStaked,
            coolingDownAmount = newCooling,
            cooldownEndTimestamp = cooldownEnd
        )

        val warning = if (isFullUnstake) {
            "Full unstake initiated. WARNING: Rewards stopped immediately for Epoch #${current.epoch}. 48-hour cooldown clock is running."
        } else {
            "Partial unstake ($amountToUnstake SKR) initiated. 48-hour cooldown clock started. Remaining $newStaked SKR continues earning 26.2% APY."
        }
        return Pair(true, warning)
    }

    fun completeCooldownWithdraw(): Pair<Boolean, String> {
        val current = _stakePosition.value
        val cooling = current.coolingDownAmount.toDoubleOrNull() ?: 0.0
        if (cooling <= 0.0) {
            return Pair(false, "No SKR currently in cooldown.")
        }
        val liquid = current.liquidAmount.toDoubleOrNull() ?: 0.0
        val newLiquid = "%.2f".format(liquid + cooling)

        _stakePosition.value = current.copy(
            coolingDownAmount = "0.00",
            liquidAmount = newLiquid,
            cooldownEndTimestamp = null
        )
        return Pair(true, "Successfully withdrew $cooling SKR to liquid wallet balance!")
    }

    fun claimSeasonAllocation(seasonId: String, currentSolBalance: Double): Pair<Boolean, String> {
        if (currentSolBalance < MINIMUM_SOL_FEE_FLOOR) {
            return Pair(false, "SOL Floor Violation: You have ${"%.4f".format(currentSolBalance)} SOL. Claims require >= 0.015 SOL for account rent and network fees.")
        }

        val claim = _seasonClaims.value.find { it.seasonId == seasonId }
            ?: return Pair(false, "Season claim not found.")

        if (claim.status != ClaimStatus.CLAIMABLE_NOW) {
            return Pair(false, "This allocation is ${claim.status.displayName}.")
        }

        val updatedClaims = _seasonClaims.value.map {
            if (it.seasonId == seasonId) it.copy(status = ClaimStatus.CLAIMED) else it
        }
        _seasonClaims.value = updatedClaims

        // Add to liquid balance
        val cur = _stakePosition.value
        val liquid = cur.liquidAmount.toDoubleOrNull() ?: 0.0
        val allocated = claim.allocatedSkr.toDoubleOrNull() ?: 0.0
        _stakePosition.value = cur.copy(
            liquidAmount = "%.2f".format(liquid + allocated)
        )

        return Pair(true, "Claimed ${claim.allocatedSkr} SKR! Transferred to liquid wallet balance.")
    }
}
