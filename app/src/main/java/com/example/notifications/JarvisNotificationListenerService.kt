package com.example.notifications

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.concurrent.CopyOnWriteArrayList

data class NotificationSummary(
    val packageName: String,
    val appName: String,
    val senderOrTitle: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Native NotificationListenerService for JARVIS.
 * Provides hands-free checking of recent messages and notifications when explicitly granted in Android Settings.
 */
class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        private val recentNotifications = CopyOnWriteArrayList<NotificationSummary>()

        fun isAccessGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, JarvisNotificationListenerService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun getLatestMessage(): NotificationSummary? {
            return recentNotifications.firstOrNull()
        }

        fun getRecentNotificationsList(): List<NotificationSummary> {
            return recentNotifications.take(5)
        }

        fun clearNotifications() {
            recentNotifications.clear()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName
        if (pkg == packageName) return // Ignore JARVIS's own notifications

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }

        val summary = NotificationSummary(
            packageName = pkg,
            appName = appName,
            senderOrTitle = title,
            messageText = text,
            timestamp = sbn.postTime
        )

        // Add to front of list and maintain max 15 items
        recentNotifications.add(0, summary)
        if (recentNotifications.size > 15) {
            recentNotifications.removeAt(recentNotifications.size - 1)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}
}
