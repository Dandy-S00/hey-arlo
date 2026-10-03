package com.example.arlo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.arlo.data.ArloRepository
import com.example.arlo.ui.screens.MainScaffold
import com.example.arlo.ui.screens.VaultScreen
import com.example.arlo.ui.theme.ArloDarkBackground
import com.example.arlo.ui.theme.ArloTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: ArloRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ArloRepository(applicationContext)

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
                            onUnlock = { passphrase ->
                                repository.unlock(passphrase)
                            },
                            onCreate = { passphrase, migrateLegacy ->
                                repository.createVault(passphrase, migrateLegacy)
                            },
                            onResetVault = {
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
                                repository.deleteVault()
                            }
                        )
                    }
                }
            }
        }
    }
}
