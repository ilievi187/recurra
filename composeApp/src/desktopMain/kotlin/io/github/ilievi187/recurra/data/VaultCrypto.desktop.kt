package io.github.ilievi187.recurra.data

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object VaultCrypto {
    private const val HEADER = "RECURRA-ENC-V1"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val GCM_TAG_BITS = 128

    actual fun encrypt(plaintext: String, passphrase: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val key = deriveKey(passphrase, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val b64 = Base64.getEncoder()
        return listOf(
            HEADER,
            b64.encodeToString(salt),
            b64.encodeToString(iv),
            b64.encodeToString(ciphertext),
        ).joinToString(":")
    }

    actual fun decrypt(payload: String, passphrase: String): String {
        val parts = payload.trim().split(":")
        require(parts.size == 4 && parts[0] == HEADER) {
            "Invalid or unsupported Recurra encrypted backup format."
        }
        val b64 = Base64.getDecoder()
        val salt = b64.decode(parts[1])
        val iv = b64.decode(parts[2])
        val ciphertext = b64.decode(parts[3])
        val key = deriveKey(passphrase, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plainBytes = cipher.doFinal(ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val encoded = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(encoded, "AES")
    }
}
