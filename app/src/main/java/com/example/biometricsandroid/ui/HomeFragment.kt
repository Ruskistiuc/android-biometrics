package com.example.biometricsandroid.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import com.example.biometricsandroid.*
import com.example.biometricsandroid.cryptography.CryptographyManager
import com.example.biometricsandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var cryptographyManager: CryptographyManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        binding.logoutBtn.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showBiometricPromptForEncryption()
            } else {
                SampleAppUser.fakeToken = null
                SampleAppUser.username = ""
                context?.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)?.edit()
                    ?.remove(CIPHER_TEXT_WRAPPER)?.apply()
            }
        }

        return binding.root
    }

    private fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
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
            SampleAppUser.fakeToken?.let { token ->
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
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
}