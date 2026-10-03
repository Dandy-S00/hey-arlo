package com.example.arlo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arlo.data.ArloRepository
import com.example.arlo.security.BiometricAuthManager
import com.example.arlo.ui.screens.MainScaffold
import com.example.arlo.ui.screens.VaultScreen
import com.example.arlo.ui.theme.ArloDarkBackground
import com.example.arlo.ui.theme.ArloTheme

class MainActivity : FragmentActivity() {
    private lateinit var repository: ArloRepository
    private lateinit var biometricManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ArloRepository(applicationContext)
        biometricManager = BiometricAuthManager(applicationContext)

        setContent {
            ArloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ArloDarkBackground
                ) {
                    val isLocked by repository.isLocked.collectAsStateWithLifecycle()
                    val hasVault by repository.hasVault.collectAsStateWithLifecycle()
                    val hasLegacyData by repository.hasLegacyData.collectAsStateWithLifecycle()
                    val state by repository.state.collectAsStateWithLifecycle()

                    if (isLocked || state == null) {
                        VaultScreen(
                            hasExistingVault = hasVault,
                            hasLegacyData = hasLegacyData,
                            isBiometricAvailable = biometricManager.isBiometricAvailable(),
                            isBiometricEnabled = biometricManager.isBiometricEnabled(),
                            onTriggerBiometric = {
                                biometricManager.authenticate(
                                    activity = this@MainActivity,
                                    onSuccess = { storedPassphrase ->
                                        repository.unlock(storedPassphrase)
                                    },
                                    onError = { /* handled in UI */ }
                                )
                            },
                            onUnlock = { passphrase, enableBiometrics ->
                                val res = repository.unlock(passphrase)
                                if (res.isSuccess && enableBiometrics) {
                                    biometricManager.enableBiometrics(passphrase)
                                }
                                res
                            },
                            onCreate = { passphrase, migrateLegacy, enableBiometrics ->
                                val res = repository.createVault(passphrase, migrateLegacy)
                                if (res.isSuccess && enableBiometrics) {
                                    biometricManager.enableBiometrics(passphrase)
                                }
                                res
                            },
                            onResetVault = {
                                biometricManager.disableBiometrics()
                                repository.deleteVault()
                            }
                        )
                    } else {
                        MainScaffold(
                            state = state!!,
                            repository = repository,
                            onLockArlo = {
                                repository.lock()
                            },
                            onResetVault = {
                                biometricManager.disableBiometrics()
                                repository.deleteVault()
                            }
                        )
                    }
                }
            }
        }
    }
}
