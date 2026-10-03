package com.example.arlo.security

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultCrypto {
    private const val PREFIX = "arlo-vault-v1:"
    private const val ITERATIONS = 600000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    private val secureRandom = SecureRandom()

    fun encrypt(plaintext: String, passphrase: CharArray): String {
        require(passphrase.size >= 12) { "Passphrase must be at least 12 characters." }

        val salt = ByteArray(SALT_LENGTH_BYTES).apply { secureRandom.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTES).apply { secureRandom.nextBytes(this) }

        val keySpec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val b64Encoder = Base64.getEncoder()
        val envelope = JSONObject().apply {
            put("v", 1)
            put("kdf", "PBKDF2-SHA-256")
            put("iterations", ITERATIONS)
            put("cipher", "AES-256-GCM")
            put("salt", b64Encoder.encodeToString(salt))
            put("iv", b64Encoder.encodeToString(iv))
            put("data", b64Encoder.encodeToString(ciphertext))
        }

        return "$PREFIX$envelope"
    }

    fun decrypt(envelopeString: String, passphrase: CharArray): String {
        require(envelopeString.startsWith(PREFIX)) { "Unsupported vault format." }

        val jsonStr = envelopeString.substring(PREFIX.length)
        val json = JSONObject(jsonStr)

        val b64Decoder = Base64.getDecoder()
        val salt = b64Decoder.decode(json.getString("salt"))
        val iv = b64Decoder.decode(json.getString("iv"))
        val ciphertext = b64Decoder.decode(json.getString("data"))
        val iterations = json.optInt("iterations", ITERATIONS)

        val keySpec = PBEKeySpec(passphrase, salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val plaintextBytes = cipher.doFinal(ciphertext)
        return String(plaintextBytes, Charsets.UTF_8)
    }
}

class VaultStorage(private val context: Context) {
    private val vaultFile: File
        get() = File(context.filesDir, "arlo-secure-vault.json")

    private val legacyPrefs = context.getSharedPreferences("arlo_legacy_prefs", Context.MODE_PRIVATE)
    private val LEGACY_KEY = "arlo-local-v2"

    fun hasVault(): Boolean = vaultFile.exists() && vaultFile.length() > 0

    fun hasLegacyData(): Boolean = legacyPrefs.contains(LEGACY_KEY)

    fun readLegacyData(): String? = legacyPrefs.getString(LEGACY_KEY, null)

    fun clearLegacyData() {
        legacyPrefs.edit().remove(LEGACY_KEY).apply()
    }

    fun readVaultEnvelope(): String? {
        if (!hasVault()) return null
        return try {
            vaultFile.readText(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    fun writeVaultEnvelope(envelope: String) {
        val tempFile = File(context.filesDir, "arlo-secure-vault.tmp")
        tempFile.writeText(envelope, Charsets.UTF_8)
        if (tempFile.renameTo(vaultFile).not()) {
            vaultFile.delete()
            tempFile.renameTo(vaultFile)
        }
    }

    fun deleteVault(): Boolean {
        return if (vaultFile.exists()) vaultFile.delete() else true
    }
}
