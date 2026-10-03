package com.example.arlo.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class SmsMessageItem(
    val id: String,
    val sender: String,
    val body: String,
    val dateMs: Long,
    val dateFormatted: String,
    val isRead: Boolean
)

data class EmailMessageItem(
    val id: String,
    val sender: String,
    val subject: String,
    val snippet: String,
    val dateMs: Long,
    val dateFormatted: String,
    val isRead: Boolean
)

class SmsAndEmailManager(private val context: Context) {

    private val _smsListFlow = MutableStateFlow<List<SmsMessageItem>>(emptyList())
    val smsListFlow: StateFlow<List<SmsMessageItem>> = _smsListFlow.asStateFlow()

    private val _emailListFlow = MutableStateFlow<List<EmailMessageItem>>(loadSimulatedOrCachedEmails())
    val emailListFlow: StateFlow<List<EmailMessageItem>> = _emailListFlow.asStateFlow()

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun refreshSmsMessages(): List<SmsMessageItem> = withContext(Dispatchers.IO) {
        if (!hasSmsPermission()) {
            return@withContext emptyList()
        }

        val list = mutableListOf<SmsMessageItem>()
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

        try {
            val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ
            )

            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 40"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addrCol = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyCol = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateCol = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val readCol = c.getColumnIndexOrThrow(Telephony.Sms.READ)

                while (c.moveToNext()) {
                    val id = c.getString(idCol)
                    val address = c.getString(addrCol) ?: "Unknown Sender"
                    val body = c.getString(bodyCol) ?: ""
                    val date = c.getLong(dateCol)
                    val read = c.getInt(readCol) == 1

                    list.add(
                        SmsMessageItem(
                            id = id,
                            sender = address,
                            body = body,
                            dateMs = date,
                            dateFormatted = sdf.format(Date(date)),
                            isRead = read
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // If Telephony cursor is restricted by environment emulator, fallback gracefully
        }

        _smsListFlow.value = list
        list
    }

    private fun loadSimulatedOrCachedEmails(): List<EmailMessageItem> {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val now = System.currentTimeMillis()

        return listOf(
            EmailMessageItem(
                id = "em-1",
                sender = "team@mindfulproductivity.org",
                subject = "Your Weekly Rhythm & Focus Digest",
                snippet = "Here is your gentle summary for the week: 4 major intentions moved forward with steady momentum.",
                dateMs = now - 3600000 * 2,
                dateFormatted = sdf.format(Date(now - 3600000 * 2)),
                isRead = true
            ),
            EmailMessageItem(
                id = "em-2",
                sender = "notifications@workspace.google.com",
                subject = "Project Milestones Review",
                snippet = "Reminder: Design review session scheduled for tomorrow morning. No urgent preparation needed.",
                dateMs = now - 3600000 * 5,
                dateFormatted = sdf.format(Date(now - 3600000 * 5)),
                isRead = false
            ),
            EmailMessageItem(
                id = "em-3",
                sender = "alex.research@calmtech.io",
                subject = "Notes on Circadian Energy Curves",
                snippet = "Attaching the PDF on optimizing creative deep work around natural sunlight exposure.",
                dateMs = now - 3600000 * 24,
                dateFormatted = sdf.format(Date(now - 3600000 * 24)),
                isRead = true
            )
        )
    }

    fun addManualEmail(sender: String, subject: String, snippet: String) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val now = System.currentTimeMillis()
        val item = EmailMessageItem(
            id = UUID.randomUUID().toString(),
            sender = sender,
            subject = subject,
            snippet = snippet,
            dateMs = now,
            dateFormatted = sdf.format(Date(now)),
            isRead = false
        )
        _emailListFlow.value = listOf(item) + _emailListFlow.value
    }
}
