package com.example.androidbiometrics.cryptography

import android.content.Context
import javax.crypto.Cipher

/** Handles encryption and decryption */
interface CryptographyManagerInterface {

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key.
     * The secret key uses [ENCRYPT_MODE][Cipher.ENCRYPT_MODE] is used.
     */
    fun getInitializedCipherForEncryption(keyName: String): Cipher

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key.
     * The secret key uses [DECRYPT_MODE][Cipher.DECRYPT_MODE] is used.
     */
    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    /** The Cipher created with [getInitializedCipherForEncryption] is used here */
    fun encryptData(plaintext: String, cipher: Cipher): CipherTextWrapper

    /** The Cipher created with [getInitializedCipherForDecryption] is used here */
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
