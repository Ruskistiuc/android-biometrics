package com.example.androidbiometrics

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Android Biometric APIs - Using Crypto Objects in Kotlin:
 * https://nik.re/posts/2019-11-30/new_android_biometric_apis
 */
object BiometricPromptUtils {

    fun createBiometricPrompt(
        activity: AppCompatActivity,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        processFail: () -> Unit,
        processErrorOrCancel: (Int) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                processSuccess(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                processFail()
            }

            /**
             * errCode: 7
             * errString: "Too many attempts. Try again later."
             *
             * errCode: 10
             * errString: "Fingerprint operation cancelled by user."
             *
             * errCode: 13
             * errString: "Cancel" this is our setNegativeButtonText from BiometricPrompt.PromptInfo
             */
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                processErrorOrCancel(errCode)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * https://developer.android.com/training/sign-in/biometric-auth
     */
    fun createPromptInfo(activity: AppCompatActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.prompt_info_title))
            setSubtitle(activity.getString(R.string.prompt_info_subtitle))
            setDescription(activity.getString(R.string.prompt_info_description))
            setConfirmationRequired(false)
            /**
             * setDeviceCredentialAllowed(boolean) and setNegativeButtonText(String)
             * Can't be applied simultaneously! See setDeviceCredentialAllowed description
             */
//            setDeviceCredentialAllowed(true)
            setNegativeButtonText(activity.getString(R.string.prompt_info_negative_button))
        }.build()
}
