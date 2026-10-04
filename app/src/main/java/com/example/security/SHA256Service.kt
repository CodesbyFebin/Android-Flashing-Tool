package com.example.security

import com.example.domain.model.Evidence
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object SHA256Service {

    fun hash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashString(input: String): String {
        return hash(input.toByteArray(Charsets.UTF_8))
    }

    fun hashFile(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun buildAndHashCanonicalManifest(
        actionId: String,
        photoSha256: String,
        note: String,
        capturedAt: Long,
        videoSha256: String? = null,
        documentSha256: String? = null
    ): Pair<String, String> {
        val canonicalJson = Evidence.canonicalManifestJson(
            actionId = actionId,
            photoSha256 = photoSha256,
            note = note,
            capturedAt = capturedAt,
            videoSha256 = videoSha256,
            documentSha256 = documentSha256
        )
        val manifestSha256 = hashString(canonicalJson)
        return Pair(canonicalJson, manifestSha256)
    }

    fun verifyIntegrity(
        actionId: String,
        photoSha256: String,
        note: String,
        capturedAt: Long,
        expectedManifestSha256: String,
        videoSha256: String? = null,
        documentSha256: String? = null
    ): Boolean {
        val (_, calculatedSha) = buildAndHashCanonicalManifest(
            actionId = actionId,
            photoSha256 = photoSha256,
            note = note,
            capturedAt = capturedAt,
            videoSha256 = videoSha256,
            documentSha256 = documentSha256
        )
        return calculatedSha.equals(expectedManifestSha256, ignoreCase = true)
    }
}
