package com.example.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MissedCallInfo(
    val callerName: String?,
    val number: String?,
    val timestamp: Long
)

/**
 * Handles checking missed calls and recent callers using standard Android CallLog APIs.
 */
class CallCheckManager(private val context: Context) {

    fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRecentMissedCalls(limit: Int = 3): List<MissedCallInfo> {
        if (!hasCallLogPermission()) return emptyList()

        val list = mutableListOf<MissedCallInfo>()
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        )
        val selection = "${CallLog.Calls.TYPE} = ?"
        val selectionArgs = arrayOf(CallLog.Calls.MISSED_TYPE.toString())
        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"

        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            cursor?.use {
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)

                while (it.moveToNext()) {
                    val name = if (nameIdx != -1) it.getString(nameIdx) else null
                    val number = if (numberIdx != -1) it.getString(numberIdx) else null
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else 0L
                    list.add(MissedCallInfo(name, number, date))
                }
            }
        } catch (_: Exception) {}

        return list
    }

    fun getMissedCallsSpokenSummary(): String {
        if (!hasCallLogPermission()) {
            return "Call log permission is required to check missed calls. Please enable it in Settings."
        }

        val missed = getRecentMissedCalls(3)
        if (missed.isEmpty()) {
            return "You have no missed calls, sir."
        }

        val first = missed.first()
        val caller = if (!first.callerName.isNullOrBlank()) first.callerName else first.number ?: "an unknown number"
        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(first.timestamp))

        return if (missed.size == 1) {
            "You have one missed call from $caller at $timeStr."
        } else {
            "You have ${missed.size} missed calls. The most recent was from $caller at $timeStr."
        }
    }
}
