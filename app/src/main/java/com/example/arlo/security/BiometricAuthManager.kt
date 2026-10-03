package com.example.arlo.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arlo_biometric_prefs", Context.MODE_PRIVATE)

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean("biometric_enabled", false) && isBiometricAvailable() && getStoredBiometricPassphrase() != null
    }

    fun enableBiometrics(passphrase: String) {
        prefs.edit()
            .putBoolean("biometric_enabled", true)
            .putString("saved_biometric_key", passphrase)
            .apply()
    }

    fun disableBiometrics() {
        prefs.edit()
            .putBoolean("biometric_enabled", false)
            .remove("saved_biometric_key")
            .apply()
    }

    fun getStoredBiometricPassphrase(): String? {
        return prefs.getString("saved_biometric_key", null)
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Arlo Vault",
        subtitle: String = "Use fingerprint or device security to open your vault",
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val storedPassphrase = getStoredBiometricPassphrase()
                if (storedPassphrase != null) {
                    onSuccess(storedPassphrase)
                } else {
                    onError("No biometric credentials registered. Please unlock with passphrase.")
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometric authentication failed. Try again or enter passphrase.")
            }
        })

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to initiate biometric prompt.")
        }
    }
}
