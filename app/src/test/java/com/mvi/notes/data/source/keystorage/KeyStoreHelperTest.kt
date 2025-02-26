package com.mvi.notes.data.source.keystorage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.mvi.notes.data.source.encrypted.keystorage.KeyStoreHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyStoreHelperTest {

    private lateinit var keyStoreHelper: KeyStoreHelper
    private lateinit var secretKey: SecretKey

    @Before
    fun setup() {
        secretKey = generateTestKey()
        keyStoreHelper = KeyStoreHelper()
    }

    @Test
    fun `encryptData should return encrypted string`() {
        val plainText = "Hello Secure World"
        val encryptedString = keyStoreHelper.encryptData(plainText)
        val decodedData = Base64.decode(encryptedString, Base64.DEFAULT)
        assert(decodedData.size > 12) // IV is included
    }

    @Test
    fun `decryptData should return original string`() {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val plainText = "Hello Secure World"
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        val iv = cipher.iv
        val encryptedString = Base64.encodeToString(iv + encryptedBytes, Base64.DEFAULT)
        val decryptedText = keyStoreHelper.decryptData(encryptedString)
        assertEquals(plainText, decryptedText)
    }

    private fun generateTestKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "test_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}
