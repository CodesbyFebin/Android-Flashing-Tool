package com.example.services

import com.example.domain.model.DecodedInstruction
import java.nio.ByteBuffer
import java.nio.ByteOrder

object SolanaDecoder {

    const val TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val SYSTEM_PROGRAM_ID = "11111111111111111111111111111111"
    const val MEMO_PROGRAM_ID = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"

    fun decodeInstruction(
        programId: String,
        keys: List<String>,
        data: ByteArray
    ): DecodedInstruction {
        return when (programId) {
            TOKEN_PROGRAM_ID -> decodeTokenInstruction(programId, keys, data)
            SYSTEM_PROGRAM_ID -> decodeSystemInstruction(programId, keys, data)
            MEMO_PROGRAM_ID -> {
                val memo = String(data, Charsets.UTF_8)
                DecodedInstruction.Memo(programId = programId, memoText = memo)
            }
            else -> DecodedInstruction.Unsupported(
                programId = programId,
                reason = "Unknown or unsupported program: $programId"
            )
        }
    }

    private fun decodeTokenInstruction(
        programId: String,
        keys: List<String>,
        data: ByteArray
    ): DecodedInstruction {
        if (data.isEmpty()) {
            return DecodedInstruction.Unsupported(programId, "Empty instruction data")
        }
        val discriminator = data[0].toInt() and 0xFF
        return when (discriminator) {
            12 -> { // TransferChecked
                if (data.size < 10) {
                    return DecodedInstruction.Unsupported(programId, "Invalid TransferChecked payload length")
                }
                // Little endian u64 at offset 1..8
                val amount = ByteBuffer.wrap(data, 1, 8).order(ByteOrder.LITTLE_ENDIAN).long
                val decimals = data[9].toInt() and 0xFF
                val source = keys.getOrElse(0) { "unknown_source" }
                val mint = keys.getOrElse(1) { "unknown_mint" }
                val dest = keys.getOrElse(2) { "unknown_dest" }
                val authority = keys.getOrElse(3) { "unknown_authority" }

                DecodedInstruction.TransferChecked(
                    programId = programId,
                    mint = mint,
                    source = source,
                    dest = dest,
                    authority = authority,
                    rawAmount = amount,
                    decimals = decimals
                )
            }
            3 -> { // Transfer
                if (data.size < 9) {
                    return DecodedInstruction.Unsupported(programId, "Invalid Transfer payload length")
                }
                val amount = ByteBuffer.wrap(data, 1, 8).order(ByteOrder.LITTLE_ENDIAN).long
                val source = keys.getOrElse(0) { "unknown_source" }
                val dest = keys.getOrElse(1) { "unknown_dest" }
                val authority = keys.getOrElse(2) { "unknown_authority" }

                DecodedInstruction.SplTransfer(
                    programId = programId,
                    source = source,
                    dest = dest,
                    authority = authority,
                    rawAmount = amount
                )
            }
            else -> DecodedInstruction.Unsupported(
                programId = programId,
                reason = "Instruction discriminator $discriminator is not permitted under strict Clearance rules"
            )
        }
    }

    private fun decodeSystemInstruction(
        programId: String,
        keys: List<String>,
        data: ByteArray
    ): DecodedInstruction {
        if (data.size < 4) {
            return DecodedInstruction.Unsupported(programId, "Invalid System Program data")
        }
        val disc = ByteBuffer.wrap(data, 0, 4).order(ByteOrder.LITTLE_ENDIAN).int
        return if (disc == 2) { // Transfer
            if (data.size < 12) {
                return DecodedInstruction.Unsupported(programId, "System Transfer payload too short")
            }
            val lamports = ByteBuffer.wrap(data, 4, 8).order(ByteOrder.LITTLE_ENDIAN).long
            val from = keys.getOrElse(0) { "unknown_from" }
            val to = keys.getOrElse(1) { "unknown_to" }
            DecodedInstruction.SystemTransfer(
                programId = programId,
                from = from,
                to = to,
                lamports = lamports
            )
        } else {
            DecodedInstruction.Unsupported(
                programId = programId,
                reason = "System instruction $disc not supported (only SOL Transfer allowed)"
            )
        }
    }

    fun buildMockTransferCheckedPayload(
        rawAmount: Long,
        decimals: Int
    ): ByteArray {
        val buffer = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(12.toByte()) // Disc 12 TransferChecked
        buffer.putLong(rawAmount)
        buffer.put(decimals.toByte())
        return buffer.array()
    }
}
