package com.example.biometricsandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.databinding.DataBindingUtil
import com.example.biometricsandroid.cryptography.CryptographyManager
import com.example.biometricsandroid.databinding.ActivityLoginBinding
import com.example.biometricsandroid.ui.MainActivity

/**
 * FROM:
 * 1) https://developer.android.com/codelabs/biometric-login#0
 * 2) https://github.com/googlecodelabs/biometric-login
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var biometricPrompt: BiometricPrompt
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        val canAuthenticate = BiometricManager.from(this).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && cipherTextWrapper != null) {
            binding.biometricsImage.visibility = View.VISIBLE
            binding.biometricsImage.setOnClickListener {
                showBiometricPromptForDecryption()
            }
        }

        login()
    }

    /**
     * The logic is kept inside onResume instead of onCreate so that
     * authorizing biometrics takes immediate effect.
     */
    override fun onResume() {
        super.onResume()

        if (cipherTextWrapper != null) {
            if (SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                /** The user has already logged in, so proceed to the rest of the app */
                // TODO BUG
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    // BIOMETRICS SECTION
    private fun showBiometricPromptForDecryption() {
        cipherTextWrapper?.let { textWrapper ->
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )

            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    this,
                    ::decryptServerTokenFromStorage
                )

            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        cipherTextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext =
                    cryptographyManager.decryptData(textWrapper.cipherText, it)
                SampleAppUser.fakeToken = plaintext

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

    private fun login() {
        binding.loginBtn.setOnClickListener {
            // Normally this method would asynchronously send this to your server and your sever
            // would return a token. For high sensitivity apps such as banking, you would keep that
            // token in transient memory similar to my SampleAppUser object. This way the user
            // must login each time they start the app.
            // In this sample, we don't call a server. Instead we use a fake token that we set
            // right here:

            SampleAppUser.username = binding.usernameEditText.text.toString()
            SampleAppUser.fakeToken = java.util.UUID.randomUUID().toString()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}