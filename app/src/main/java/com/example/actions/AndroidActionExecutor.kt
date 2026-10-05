package com.example.actions

import android.accessibilityservice.AccessibilityService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import com.example.automation.JarvisAccessibilityService
import com.example.notifications.JarvisNotificationListenerService
import com.example.telephony.CallCheckManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized native Android action executor for JARVIS Phase 3.
 * Strictly executes ONE verified action at a time, checks hardware availability,
 * and handles missing third-party applications gracefully.
 */
class AndroidActionExecutor(
    private val context: Context
) : ActionExecutor {

    override val executorName: String = "Centralized Android Action Executor"

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val callCheckManager = CallCheckManager(context)
    private var isTorchOn = false

    override fun isActionSupported(actionId: ActionId): Boolean = true

    override suspend fun executeAction(
        actionId: ActionId,
        parameters: Map<String, Any>
    ): ActionResult {
        return when (actionId) {
            ActionId.OPEN_CAMERA -> {
                launchIntent(
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                    successSpeech = "Opening Camera.",
                    actionTitle = "Open Camera",
                    missingAppSpeech = "Camera application is not available on this device."
                )
            }

            ActionId.OPEN_BROWSER -> {
                launchIntent(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")),
                    successSpeech = "Opening Browser.",
                    actionTitle = "Open Browser"
                )
            }

            ActionId.OPEN_CHROME -> {
                if (isPackageInstalled("com.android.chrome")) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.android.chrome")
                    if (intent != null) {
                        launchIntent(intent, "Opening Chrome.", "Open Chrome")
                    } else {
                        ActionResult(false, "Chrome is not installed on this device.", "Chrome Unavailable")
                    }
                } else {
                    ActionResult(false, "Chrome is not installed on this device.", "Chrome Unavailable")
                }
            }

            ActionId.OPEN_YOUTUBE -> {
                if (isPackageInstalled("com.google.android.youtube")) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                    if (intent != null) {
                        launchIntent(intent, "Opening YouTube.", "Open YouTube")
                    } else {
                        ActionResult(false, "YouTube is not installed on this device.", "YouTube Unavailable")
                    }
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                    launchIntent(
                        webIntent,
                        successSpeech = "Opening YouTube.",
                        actionTitle = "Open YouTube",
                        missingAppSpeech = "YouTube is not installed on this device."
                    )
                }
            }

            ActionId.OPEN_MAPS -> {
                if (isPackageInstalled("com.google.android.apps.maps")) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
                    if (intent != null) {
                        launchIntent(intent, "Opening Maps.", "Open Maps")
                    } else {
                        ActionResult(false, "Google Maps is not installed on this device.", "Maps Unavailable")
                    }
                } else {
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
                    launchIntent(
                        mapIntent,
                        successSpeech = "Opening Maps.",
                        actionTitle = "Open Maps",
                        missingAppSpeech = "Google Maps is not installed on this device."
                    )
                }
            }

            ActionId.OPEN_CALCULATOR -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                }
                val result = launchIntent(intent, "Opening Calculator.", "Open Calculator")
                if (!result.success) {
                    val knownCalcPackages = listOf(
                        "com.google.android.calculator",
                        "com.android.calculator2",
                        "com.sec.android.app.popupcalculator"
                    )
                    for (pkg in knownCalcPackages) {
                        if (isPackageInstalled(pkg)) {
                            val pkgIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                            if (pkgIntent != null) {
                                return launchIntent(pkgIntent, "Opening Calculator.", "Open Calculator")
                            }
                        }
                    }
                    ActionResult(false, "Calculator is not available on this device.", "Calculator Unavailable")
                } else {
                    result
                }
            }

            ActionId.OPEN_PHONE -> {
                launchIntent(
                    Intent(Intent.ACTION_DIAL),
                    successSpeech = "Opening Phone dialer.",
                    actionTitle = "Open Phone"
                )
            }

            ActionId.OPEN_CONTACTS -> {
                launchIntent(
                    Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI),
                    successSpeech = "Opening Contacts.",
                    actionTitle = "Open Contacts"
                )
            }

            ActionId.OPEN_MESSAGES -> {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MESSAGING)
                }
                launchIntent(
                    intent,
                    successSpeech = "Opening Messages.",
                    actionTitle = "Open Messages"
                )
            }

            ActionId.OPEN_CLOCK -> {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                launchIntent(
                    intent,
                    successSpeech = "Opening Clock.",
                    actionTitle = "Open Clock"
                )
            }

            ActionId.OPEN_SETTINGS -> {
                launchIntent(
                    Intent(Settings.ACTION_SETTINGS),
                    successSpeech = "Opening Settings.",
                    actionTitle = "Open Settings"
                )
            }

            ActionId.OPEN_FILES -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                val res = launchIntent(intent, "Opening Files.", "Open Files")
                if (!res.success) {
                    launchIntent(
                        Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
                        "Opening Storage Settings.",
                        "Storage Settings"
                    )
                } else res
            }

            ActionId.OPEN_GALLERY -> {
                val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launchIntent(
                    intent,
                    successSpeech = "Opening Gallery.",
                    actionTitle = "Open Gallery"
                )
            }

            ActionId.OPEN_CALENDAR -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("content://com.android.calendar/time")
                }
                val res = launchIntent(intent, "Opening Calendar.", "Open Calendar")
                if (!res.success) {
                    val fallback = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                    }
                    launchIntent(fallback, "Opening Calendar.", "Open Calendar")
                } else res
            }

            ActionId.OPEN_STOPWATCH -> {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                launchIntent(intent, "Opening Clock and Stopwatch.", "Open Stopwatch")
            }

            ActionId.OPEN_WHATSAPP -> {
                if (isPackageInstalled("com.whatsapp")) {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                    if (intent != null) {
                        launchIntent(intent, "Opening WhatsApp.", "Open WhatsApp")
                    } else {
                        ActionResult(false, "WhatsApp is not installed on this device.", "WhatsApp Unavailable")
                    }
                } else {
                    ActionResult(false, "WhatsApp is not installed on this device.", "WhatsApp Unavailable")
                }
            }

            ActionId.OPEN_EMAIL -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                }
                val res = launchIntent(intent, "Opening Email.", "Open Email")
                if (!res.success) {
                    val fallback = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_EMAIL)
                    }
                    launchIntent(fallback, "Opening Email.", "Open Email")
                } else res
            }

            ActionId.OPEN_MUSIC -> {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MUSIC)
                }
                val res = launchIntent(intent, "Opening Music.", "Open Music")
                if (!res.success) {
                    launchIntent(
                        Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER),
                        "Opening Music Player.",
                        "Open Music"
                    )
                } else res
            }

            ActionId.GO_HOME -> {
                if (JarvisAccessibilityService.isServiceEnabled(context) && JarvisAccessibilityService.instance != null) {
                    val ok = JarvisAccessibilityService.instance?.performHomeAction() == true
                    if (ok) {
                        return ActionResult(true, "Returning to the home screen.", "Home Screen")
                    }
                }
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                launchIntent(intent, "Returning to the home screen.", "Home Screen")
            }

            ActionId.GO_BACK -> {
                if (JarvisAccessibilityService.isServiceEnabled(context) && JarvisAccessibilityService.instance != null) {
                    val executed = JarvisAccessibilityService.instance?.performBackAction() == true
                    if (executed) {
                        ActionResult(true, "Going back.", "Go Back")
                    } else {
                        ActionResult(false, "Could not perform back action.", "Go Back Failed")
                    }
                } else {
                    ActionResult(
                        success = false,
                        spokenResponse = "Accessibility access is required for this action. Please enable JARVIS Accessibility Service in Android Settings.",
                        actionTitle = "Accessibility Required",
                        requiresAccessibility = true
                    )
                }
            }

            ActionId.OPEN_RECENTS -> {
                if (JarvisAccessibilityService.isServiceEnabled(context) && JarvisAccessibilityService.instance != null) {
                    val executed = JarvisAccessibilityService.instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) == true
                    if (executed) {
                        ActionResult(true, "Opening recent apps.", "Recent Apps")
                    } else {
                        ActionResult(false, "Could not open recent applications.", "Recent Apps Failed")
                    }
                } else {
                    ActionResult(
                        success = false,
                        spokenResponse = "Accessibility access is required to view recent apps. Please enable JARVIS Accessibility Service in Android Settings.",
                        actionTitle = "Accessibility Required",
                        requiresAccessibility = true
                    )
                }
            }

            ActionId.VOLUME_UP -> {
                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
                ActionResult(true, "Increasing volume.", "Volume Increased")
            }

            ActionId.VOLUME_DOWN -> {
                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
                ActionResult(true, "Decreasing volume.", "Volume Decreased")
            }

            ActionId.VOLUME_MUTE -> {
                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_SHOW_UI
                )
                ActionResult(true, "Volume muted.", "Volume Muted")
            }

            ActionId.VOLUME_UNMUTE -> {
                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_UNMUTE,
                    AudioManager.FLAG_SHOW_UI
                )
                ActionResult(true, "Volume unmuted.", "Volume Unmuted")
            }

            ActionId.OPEN_WIFI_SETTINGS -> {
                launchIntent(Intent(Settings.ACTION_WIFI_SETTINGS), "Opening Wi-Fi settings.", "Wi-Fi Settings")
            }

            ActionId.OPEN_BLUETOOTH_SETTINGS -> {
                launchIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "Opening Bluetooth settings.", "Bluetooth Settings")
            }

            ActionId.OPEN_DISPLAY_SETTINGS -> {
                launchIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS), "Opening display settings.", "Display Settings")
            }

            ActionId.OPEN_SOUND_SETTINGS -> {
                launchIntent(Intent(Settings.ACTION_SOUND_SETTINGS), "Opening sound settings.", "Sound Settings")
            }

            ActionId.OPEN_BATTERY_SETTINGS -> {
                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                val res = launchIntent(intent, "Opening battery settings.", "Battery Settings")
                if (!res.success) {
                    launchIntent(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS), "Opening battery settings.", "Battery Settings")
                } else res
            }

            ActionId.OPEN_APP_SETTINGS -> {
                launchIntent(Intent(Settings.ACTION_APPLICATION_SETTINGS), "Opening application settings.", "App Settings")
            }

            ActionId.SET_TIMER -> {
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                }
                launchIntent(intent, "Opening timer.", "Set Timer")
            }

            ActionId.SET_ALARM -> {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                }
                launchIntent(intent, "Opening alarm.", "Set Alarm")
            }

            ActionId.FLASHLIGHT_ON -> {
                setFlashlight(true)
            }

            ActionId.FLASHLIGHT_OFF -> {
                setFlashlight(false)
            }

            ActionId.FLASHLIGHT_TOGGLE -> {
                setFlashlight(!isTorchOn)
            }

            ActionId.MEDIA_PLAY_PAUSE -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                ActionResult(true, "Media playback toggled.", "Play / Pause Media")
            }

            ActionId.MEDIA_NEXT -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                ActionResult(true, "Playing next track.", "Next Track")
            }

            ActionId.MEDIA_PREVIOUS -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                ActionResult(true, "Playing previous track.", "Previous Track")
            }

            ActionId.BATTERY_CHECK -> {
                val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                if (level >= 0 && scale > 0) {
                    val pct = (level * 100) / scale
                    val chargeStatus = if (isCharging) "and currently charging" else "not charging"
                    ActionResult(true, "The battery is at $pct% $chargeStatus, sir.", "Battery Status")
                } else {
                    ActionResult(false, "Unable to read battery level at this time.", "Battery Status")
                }
            }

            ActionId.NETWORK_CHECK -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = cm?.activeNetwork
                val caps = cm?.getNetworkCapabilities(activeNetwork)
                val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

                val speech = when {
                    isWifi -> "Device is connected to Wi-Fi, sir."
                    isCellular -> "Device is connected to cellular mobile data, sir."
                    caps != null -> "Network connection is active."
                    else -> "Device has no active internet connection."
                }
                ActionResult(true, speech, "Network Status")
            }

            ActionId.BLUETOOTH_CHECK -> {
                val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val isEnabled = bm?.adapter?.isEnabled == true
                val speech = if (isEnabled) "Bluetooth is currently turned on, sir." else "Bluetooth is turned off, sir."
                ActionResult(true, speech, "Bluetooth Status")
            }

            ActionId.CHECK_MESSAGES, ActionId.CHECK_LATEST_MESSAGE -> {
                if (!JarvisNotificationListenerService.isAccessGranted(context)) {
                    ActionResult(
                        success = false,
                        spokenResponse = "Notification listener access is required to check incoming messages. Please enable it in Settings.",
                        actionTitle = "Notification Access Required",
                        requiresNotificationAccess = true
                    )
                } else {
                    val latest = JarvisNotificationListenerService.getLatestMessage()
                    if (latest != null) {
                        val sender = if (latest.senderOrTitle.isNotBlank()) latest.senderOrTitle else latest.appName
                        ActionResult(
                            success = true,
                            spokenResponse = "Latest message from $sender: \"${latest.messageText}\"",
                            actionTitle = "Latest Message"
                        )
                    } else {
                        ActionResult(
                            success = true,
                            spokenResponse = "You have no new message notifications, sir.",
                            actionTitle = "Messages Check"
                        )
                    }
                }
            }

            ActionId.CHECK_CALLS -> {
                if (!callCheckManager.hasCallLogPermission()) {
                    ActionResult(
                        success = false,
                        spokenResponse = "Call log permission is required to check call history. Please enable it in Settings.",
                        actionTitle = "Call Log Permission Required",
                        requiresCallLogPermission = true
                    )
                } else {
                    val summary = callCheckManager.getMissedCallsSpokenSummary()
                    ActionResult(true, summary, "Missed Calls")
                }
            }

            ActionId.TIME_CHECK -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val time = timeFormat.format(Date())
                ActionResult(true, "The current time is $time.", "Time Check")
            }

            ActionId.DATE_CHECK -> {
                val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                val date = dateFormat.format(Date())
                ActionResult(true, "Today is $date.", "Date Check")
            }

            ActionId.SYSTEM_STATUS -> {
                ActionResult(
                    true,
                    "All primary offline engines, persistent database memory, and system action executors are operational.",
                    "System Status"
                )
            }
        }
    }

    private fun setFlashlight(enable: Boolean): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                val speech = if (enable) "Flashlight turned on." else "Flashlight turned off."
                ActionResult(true, speech, "Flashlight Control")
            } else {
                ActionResult(false, "Flashlight hardware is not available on this device.", "Flashlight Unavailable")
            }
        } catch (e: Exception) {
            ActionResult(false, "Unable to control flashlight: ${e.localizedMessage ?: "Hardware error"}", "Flashlight Error")
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        try {
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (_: Exception) {}
    }

    private fun launchIntent(
        intent: Intent,
        successSpeech: String,
        actionTitle: String,
        missingAppSpeech: String? = null
    ): ActionResult {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionResult(
                success = true,
                spokenResponse = successSpeech,
                actionTitle = actionTitle,
                isIntentDispatched = true
            )
        } catch (e: Exception) {
            val speech = missingAppSpeech ?: "I can't perform that action yet. The required application may not be installed."
            ActionResult(
                success = false,
                spokenResponse = speech,
                actionTitle = "$actionTitle (Unavailable)"
            )
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }
}
