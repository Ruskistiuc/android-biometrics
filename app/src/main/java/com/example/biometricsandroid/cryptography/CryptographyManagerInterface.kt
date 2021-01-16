package com.example.biometricsandroid.cryptography

import android.content.Context
import com.example.biometricsandroid.CipherTextWrapper
import javax.crypto.Cipher

/**
 * Handles encryption and decryption
 */
interface CryptographyManager {

    fun getInitializedCipherForEncryption(keyName: String): Cipher

    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    /**
     * The Cipher created with [getInitializedCipherForEncryption] is used here
     */
    fun encryptData(plaintext: String, cipher: Cipher): CipherTextWrapper

    /**
     * The Cipher created with [getInitializedCipherForDecryption] is used here
     */
    fun decryptData(cipherText: ByteArray, cipher: Cipher): String

    fun persistCipherTextWrapperToSharedPrefs(
        cipherTextWrapper: CipherTextWrapper,
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    )

    fun getCipherTextWrapperFromSharedPrefs(
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ): CipherTextWrapper?
}