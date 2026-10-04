package com.example.domain.model

data class Evidence(
    val actionId: String,
    val photoPath: String, // Local file path or content URI
    val photoSha256: String, // SHA-256 of photo bytes
    val videoPath: String? = null,
    val videoSha256: String? = null,
    val documentPath: String? = null,
    val documentSha256: String? = null,
    val documentName: String? = null,
    val note: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val manifestSha256: String, // SHA-256 of canonical manifest
    val deviceAttestationSignature: String = "" // Android Keystore hardware-backed signature
) {
    val totalArtifactsCount: Int
        get() {
            var count = 0
            if (photoSha256.isNotBlank()) count++
            if (!videoSha256.isNullOrBlank()) count++
            if (!documentSha256.isNullOrBlank()) count++
            return count.coerceAtLeast(1)
        }

    companion object {
        fun canonicalManifestJson(
            actionId: String,
            photoSha256: String,
            note: String,
            capturedAt: Long,
            videoSha256: String? = null,
            documentSha256: String? = null
        ): String {
            val list = mutableListOf<Pair<String, String>>()
            list.add("actionId" to actionId)
            list.add("photoSha256" to photoSha256)
            if (!videoSha256.isNullOrBlank()) {
                list.add("videoSha256" to videoSha256)
            }
            if (!documentSha256.isNullOrBlank()) {
                list.add("documentSha256" to documentSha256)
            }
            list.add("note" to note)

            val arrayElements = list.joinToString(",") { (k, v) ->
                "[\"${escapeJson(k)}\",\"${escapeJson(v)}\"]"
            }
            return "[$arrayElements,[\"capturedAt\",$capturedAt]]"
        }

        private fun escapeJson(input: String): String {
            return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        }
    }
}
