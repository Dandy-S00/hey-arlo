package com.example.arlo

import com.example.arlo.security.VaultCrypto
import org.junit.Assert.*
import org.junit.Test

class VaultCryptoTest {

    @Test
    fun testEncryptDecryptRoundTrip() {
        val plaintext = """{"goals":[{"title":"Make today count"}],"message":"Hello Arlo"}"""
        val passphrase = "correct-horse-battery-staple".toCharArray()

        val envelope = VaultCrypto.encrypt(plaintext, passphrase)
        assertTrue(envelope.startsWith("arlo-vault-v1:"))

        val decrypted = VaultCrypto.decrypt(envelope, passphrase)
        assertEquals(plaintext, decrypted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPassphraseTooShortThrows() {
        VaultCrypto.encrypt("test", "short".toCharArray())
    }

    @Test
    fun testWrongPassphraseFails() {
        val plaintext = """{"test": true}"""
        val passphrase = "valid-passphrase-123".toCharArray()
        val envelope = VaultCrypto.encrypt(plaintext, passphrase)

        val wrongPass = "wrong-passphrase-456".toCharArray()
        try {
            VaultCrypto.decrypt(envelope, wrongPass)
            fail("Expected exception for wrong passphrase")
        } catch (e: Exception) {
            // expected
            assertTrue(e.message != null || e is javax.crypto.AEADBadTagException)
        }
    }
}
