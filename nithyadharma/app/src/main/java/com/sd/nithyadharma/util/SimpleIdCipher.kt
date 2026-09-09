package com.sd.nithyadharma.util

import android.util.Base64

object SimpleIdCipher {

    // 💡 Lightweight secret salt used for XOR shifting
    private const val SECRET_KEY = "ND_USER_SALT_2026"

    /**
     * Reversibly encrypts plain text (e.g., "Ramesh") into a lightweight URL-safe ID.
     */
    fun encrypt(input: String): String {
        if (input.isBlank()) return ""

        val keyBytes = SECRET_KEY.toByteArray(Charsets.UTF_8)
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        val xorBytes = ByteArray(inputBytes.size)

        for (i in inputBytes.indices) {
            xorBytes[i] = (inputBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }

        // URL_SAFE + NO_WRAP ensures the resulting string works cleanly as a Firebase User ID
        return Base64.encodeToString(
            xorBytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    /**
     * Decrypts the generated ID back into the original plain text name.
     */
    fun decrypt(encryptedId: String): String {
        if (encryptedId.isBlank()) return ""

        return try {
            val xorBytes = Base64.decode(
                encryptedId,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val keyBytes = SECRET_KEY.toByteArray(Charsets.UTF_8)
            val outputBytes = ByteArray(xorBytes.size)

            for (i in xorBytes.indices) {
                outputBytes[i] = (xorBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }

            String(outputBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "" // Return empty string if decryption fails or format is invalid
        }
    }
}