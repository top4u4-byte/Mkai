package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import java.lang.ref.WeakReference

/**
 * Concrete Android AccessibilityService foundation for JARVIS automation.
 * Allows safe execution of system navigation gestures (Go Back, Go Home) and prepares
 * inspection of UI windows when explicitly requested by voice commands.
 */
class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private var instanceRef: WeakReference<JarvisAccessibilityService>? = null

        val instance: JarvisAccessibilityService?
            get() = instanceRef?.get()

        /**
         * Checks whether JARVIS Accessibility Service is currently enabled by the user in Android Settings.
         */
        fun isServiceEnabled(context: Context): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            val expectedPackage = context.packageName
            for (service in enabledServices) {
                val serviceInfo = service.resolveInfo.serviceInfo
                if (serviceInfo.packageName == expectedPackage &&
                    serviceInfo.name == JarvisAccessibilityService::class.java.name
                ) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Foundation for future UI observation & screen understanding
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instanceRef?.get() == this) {
            instanceRef = null
        }
    }

    /**
     * Executes the system Back action safely through the Accessibility framework.
     */
    fun performBackAction(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /**
     * Executes the system Home action safely through the Accessibility framework.
     */
    fun performHomeAction(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
}
