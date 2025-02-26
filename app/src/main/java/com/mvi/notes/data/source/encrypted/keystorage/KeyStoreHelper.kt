package com.mvi.notes.data.source.encrypted.keystorage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Singleton

@Singleton
class KeyStoreHelper  {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /**
     * Generates a new AES encryption key if it does not already exist.
     */
    fun generateKey() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Retrieves the AES encryption key from Android's KeyStore.
     */
    private fun getSecretKey(): SecretKey {
        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    /**
     * Encrypts data using the AES key from the KeyStore.
     */
    fun encryptData(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(iv + encryptedBytes, Base64.DEFAULT)
    }

    /**
     * Decrypts data using the AES key from the KeyStore.
     */
    fun decryptData(encryptedData: String): String {
        val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = decodedData.copyOfRange(0, IV_SIZE)
        val encryptedBytes = decodedData.copyOfRange(IV_SIZE, decodedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(TAG_LENGTH, iv))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    /**
     * Checks if the key has been generated in the KeyStore.
     */
    fun isKeyGenerated(): Boolean {
        return try {
            val key = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            key != null
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "secure_notes_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_LENGTH = 128
    }
}
