package io.github.ilievi187.recurra.data

/**
 * Authenticated symmetric encryption for portable offline backups.
 * Implemented using AES-256-GCM with PBKDF2WithHmacSHA256 (120,000 iterations),
 * random 16-byte salt, and random 12-byte IV.
 */
expect object VaultCrypto {
    fun encrypt(plaintext: String, passphrase: String): String
    fun decrypt(payload: String, passphrase: String): String
}
