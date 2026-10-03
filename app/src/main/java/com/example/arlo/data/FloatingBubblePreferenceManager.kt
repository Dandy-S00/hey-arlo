package com.example.arlo.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.arlo.service.ArloFloatingBubbleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FloatingBubblePreferenceManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("arlo_floating_prefs", Context.MODE_PRIVATE)

    private val _isBubbleEnabled = MutableStateFlow(prefs.getBoolean("floating_bubble_enabled", false))
    val isBubbleEnabled: StateFlow<Boolean> = _isBubbleEnabled.asStateFlow()

    fun hasOverlayPermission(): Boolean {
        return ArloFloatingBubbleService.isOverlayAvailable(context)
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun setBubbleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("floating_bubble_enabled", enabled).apply()
        _isBubbleEnabled.value = enabled
        if (enabled) {
            if (hasOverlayPermission()) {
                ArloFloatingBubbleService.startService(context)
            } else {
                requestOverlayPermission()
            }
        } else {
            ArloFloatingBubbleService.stopService(context)
        }
    }
}
