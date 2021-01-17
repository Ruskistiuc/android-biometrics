package com.example.biometricsandroid.ui

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
import com.example.biometricsandroid.*
import com.example.biometricsandroid.cryptography.CryptographyManager
import com.example.biometricsandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var cryptographyManager: CryptographyManager
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
                // showBiometricPromptForEncryption()
                Toast.makeText(requireContext(), "Show biometric prompt", Toast.LENGTH_SHORT).show()
            } else {
                // Remove token from sharedPreferences
                sharedPreferences.edit().remove(CIPHER_TEXT_WRAPPER).apply()
            }
        }
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

    private fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            // TODO what is secretKeyName ?
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = CryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    activity as MainActivity,
                    ::encryptAndStoreServerToken
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(activity as MainActivity)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            // 1) Get current plain text token
            val token = sharedPreferences.getString(TOKEN, null)

            // 2) Encrypt current token
            val encryptedServerTokenWrapper = cryptographyManager.encryptData(token!!, this)

            // 3) Save encrypted token to sharedPreferences
            cryptographyManager.persistCipherTextWrapperToSharedPrefs(
                encryptedServerTokenWrapper,
                requireContext(),
                SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE,
                CIPHER_TEXT_WRAPPER
            )
        }
    }
}