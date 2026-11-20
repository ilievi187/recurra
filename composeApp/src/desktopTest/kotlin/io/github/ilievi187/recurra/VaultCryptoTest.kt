package io.github.ilievi187.recurra

import io.github.ilievi187.recurra.data.VaultCrypto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class VaultCryptoTest {

    @Test
    fun encryptsAndDecryptsAes256GcmPayload() {
        val secret = """{"schemaVersion":1,"subscriptions":[{"name":"Proton"}]}"""
        val pass = "correct-horse-battery-staple"

        val enc1 = VaultCrypto.encrypt(secret, pass)
        val enc2 = VaultCrypto.encrypt(secret, pass)

        // Random salt & IV ensure distinct ciphertexts
        assertNotEquals(enc1, enc2)
        assertEquals(secret, VaultCrypto.decrypt(enc1, pass))
        assertEquals(secret, VaultCrypto.decrypt(enc2, pass))
    }

    @Test
    fun rejectsWrongPassphraseOrTamperedCiphertext() {
        val enc = VaultCrypto.encrypt("sensitive-vault-data", "strong-pass-123")
        assertFailsWith<Exception> {
            VaultCrypto.decrypt(enc, "wrong-pass-456")
        }
        assertFailsWith<Exception> {
            VaultCrypto.decrypt(enc.dropLast(2) + "AA", "strong-pass-123")
        }
    }
}
