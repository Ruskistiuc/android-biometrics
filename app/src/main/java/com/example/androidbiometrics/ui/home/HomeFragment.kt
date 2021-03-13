package com.example.androidbiometrics.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import com.example.androidbiometrics.BiometricPromptUtils
import com.example.androidbiometrics.CIPHER_TEXT_WRAPPER
import com.example.androidbiometrics.IS_BIOMETRIC_ENABLED
import com.example.androidbiometrics.LoginActivity
import com.example.androidbiometrics.R
import com.example.androidbiometrics.SHARED_PREFS_FILENAME
import com.example.androidbiometrics.TOKEN
import com.example.androidbiometrics.USERNAME
import com.example.androidbiometrics.cryptography.CryptographyManagerInterface
import com.example.androidbiometrics.cryptography.getCryptographyManager
import com.example.androidbiometrics.databinding.FragmentHomeBinding
import com.example.androidbiometrics.ui.MainActivity

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var cryptographyManager: CryptographyManagerInterface
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        setupLayout()
        onBiometricSwitchListener()
        logout()

        return binding.root
    }

    private fun setupLayout() {
        // Username
        val username = sharedPreferences.getString(USERNAME, "John Doe")
        binding.welcomeUser.text = requireContext().getString(R.string.welcome_user, username)

        // Biometric switch
        val isBiometricEnabled = sharedPreferences.getBoolean(IS_BIOMETRIC_ENABLED, false)
        binding.biometricSwitch.isChecked = isBiometricEnabled
    }

    private fun onBiometricSwitchListener() {
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(IS_BIOMETRIC_ENABLED, isChecked).apply()
            if (isChecked) {
                showBiometricPromptForEncryption()
            } else {
                // Remove cipher token from sharedPreferences
                sharedPreferences.edit().remove(CIPHER_TEXT_WRAPPER).apply()
            }
        }
    }

    private fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = getCryptographyManager()
            val cipher =
                cryptographyManager.getInitializedCipherForEncryption(secretKeyName)

            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    activity as MainActivity,
                    ::onSuccess,
                    ::onFail,
                    ::onErrorOrCancel
                )

            val promptInfo = BiometricPromptUtils.createPromptInfo(activity as MainActivity)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))

            // TODO in order to support setDeviceCredentialAllowed inside createPromptInfo we need to ignore CryptoObject
            // biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun onSuccess(authResult: BiometricPrompt.AuthenticationResult) {
        /** Encrypt and store server token */
        authResult.cryptoObject?.cipher?.apply {
            // 1) Get current plain text token
            val token = sharedPreferences.getString(TOKEN, null)

            // 2) Encrypt current token
            val encryptedServerTokenWrapper =
                cryptographyManager.encryptData(token!!, this)

            // 3) Save encrypted token to sharedPreferences
            cryptographyManager.persistCipherTextWrapperToSharedPrefs(
                encryptedServerTokenWrapper,
                requireContext(),
                SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE,
                CIPHER_TEXT_WRAPPER
            )

            Toast.makeText(
                requireContext(),
                "Biometrics configured successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onErrorOrCancel(errCode: Int) {
        // Unchecked Biometric switch
        binding.biometricSwitch.isChecked = false

        Toast.makeText(
            requireContext(),
            "ERROR OR CANCEL with error code $errCode",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun onFail() {
        Toast.makeText(requireContext(), "FAILED", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        binding.logoutBtn.setOnClickListener {
            // Delete all data saved in sharedPreferences
            sharedPreferences.edit().clear().apply()

            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
