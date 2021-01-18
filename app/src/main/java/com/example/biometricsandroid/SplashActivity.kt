package com.example.biometricsandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.biometricsandroid.cryptography.CryptographyManager
import com.example.biometricsandroid.databinding.ActivitySplashBinding
import com.example.biometricsandroid.ui.MainActivity
import kotlinx.coroutines.delay

/**
 * FROM:
 * 1) https://developer.android.com/codelabs/biometric-login#0
 * 2) https://github.com/googlecodelabs/biometric-login
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var binding: ActivitySplashBinding

    private val cryptographyManager = CryptographyManager()
    private val cipherTextWrapper
        get() = cryptographyManager.getCipherTextWrapperFromSharedPrefs(
            this,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHER_TEXT_WRAPPER
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        // Just to simulate background work while showing SplashActivity
        lifecycleScope.launchWhenCreated {
            delay(1500)
            auth()
        }
    }

    private fun auth() {
        val canAuthenticate = BiometricManager.from(this).canAuthenticate()

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            /** Biometric can currently be used */
            if (cipherTextWrapper != null) {
                /** If the Biometric was already enrolled (NOT FIRST TIME APP OPENING)*/
                showBiometricPromptForDecryption()
            } else {
                /** Biometric can be used but is not enrolled yet (FIRST TIME APP OPENING)*/
                // Navigate to LoginActivity in order to insert the credentials
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        } else {
            /**
             * 1) User does not have any enrolled biometric
             * 2) Biometric is not currently enabled/supported
             */
            // Navigate to LoginActivity in order to insert the credentials
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun showBiometricPromptForDecryption() {
        cipherTextWrapper?.let { textWrapper ->
            // TODO what is secretKeyName ? Get Secret Key alias from the app's memory
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )

            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    this,
                    ::onSuccess,
                    ::onFail,
                    ::onErrorOrPIN
                )

            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun onSuccess(authResult: BiometricPrompt.AuthenticationResult) {
        /** Decrypt server token from storage */
        cipherTextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                // 1) Decrypt saved cipher token
                val token =
                    cryptographyManager.decryptData(textWrapper.cipherText, it)

                // 2) Save plain text token to sharedPreferences
                getSharedPreferences(SHARED_PREFS_FILENAME, MODE_PRIVATE).edit()
                    .putString(TOKEN, token).apply()

                /**
                 * Now that you have the token, you can query server for everything else the only
                 * reason we call this fakeToken is because we didn't really get it from the server.
                 * In your case, you will have gotten it from the server the first time
                 * and therefore, it's a real token.
                 */

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun onFail() {
        Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show()
    }

    private fun onErrorOrPIN(errCode: Int) {
        Toast.makeText(this, "ERROR OR PIN", Toast.LENGTH_SHORT).show()
    }
}