package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardware-backed secure storage for the user's Gemini API key using AndroidKeyStore and AES-256 GCM.
 * The key is encrypted before persisting and never logged.
 */
class SecureApiKeyStorage(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "jarvis_secure_prefs"
        private const val KEY_ALIAS = "jarvis_gemini_api_key_alias"
        private const val PREF_ENCRYPTED_KEY = "encrypted_api_key"
        private const val PREF_IV = "encryption_iv"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(parameterSpec)
            return keyGenerator.generateKey()
        }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * Encrypts and securely stores the user's Gemini API key.
     */
    fun saveApiKey(apiKey: String) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) return

        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(trimmedKey.toByteArray(Charsets.UTF_8))

            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

            sharedPreferences.edit()
                .putString(PREF_ENCRYPTED_KEY, encryptedBase64)
                .putString(PREF_IV, ivBase64)
                .apply()
        } catch (_: Exception) {
            // Fail safely without logging key content
        }
    }

    /**
     * Decrypts and retrieves the stored API key, or empty string if not present.
     */
    fun getApiKey(): String {
        val encryptedBase64 = sharedPreferences.getString(PREF_ENCRYPTED_KEY, null) ?: return ""
        val ivBase64 = sharedPreferences.getString(PREF_IV, null) ?: return ""

        return try {
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Checks whether a valid non-empty API key is saved.
     */
    fun hasApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    /**
     * Deletes the stored API key and removes the encrypted data from preferences.
     */
    fun clearApiKey() {
        sharedPreferences.edit()
            .remove(PREF_ENCRYPTED_KEY)
            .remove(PREF_IV)
            .apply()
    }
}
