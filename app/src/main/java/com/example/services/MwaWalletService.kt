package com.example.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom

data class ConnectedWallet(
    val address: String,
    val cluster: String = "solana:devnet",
    val walletName: String = "Seeker Seed Vault",
    val isConnected: Boolean = true
)

class MwaWalletService {

    // Default primary testnet key matching Solana Seeker specifications
    private val defaultAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU"

    private val _walletState = MutableStateFlow<ConnectedWallet?>(
        ConnectedWallet(
            address = defaultAddress,
            cluster = "solana:devnet",
            walletName = "Seeker Seed Vault",
            isConnected = true
        )
    )
    val walletState: StateFlow<ConnectedWallet?> = _walletState.asStateFlow()

    suspend fun connect(walletName: String = "Seeker Seed Vault"): ConnectedWallet {
        delay(400) // Realistic handshake animation
        val wallet = ConnectedWallet(
            address = defaultAddress,
            cluster = "solana:devnet",
            walletName = walletName,
            isConnected = true
        )
        _walletState.value = wallet
        return wallet
    }

    fun disconnect() {
        _walletState.value = null
    }

    suspend fun signTransaction(messagePayload: ByteArray): String {
        delay(600) // Simulated secure TEE signing delay
        val random = ByteArray(32)
        SecureRandom().nextBytes(random)
        val sigHex = random.joinToString("") { "%02x".format(it) }
        return "5Z" + sigHex.take(64) + "K" // Format standard Solana base58 signature style
    }
}
