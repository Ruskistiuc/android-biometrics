package com.example.biometricsandroid.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.biometricsandroid.R
import com.example.biometricsandroid.SHARED_PREFS_FILENAME
import com.example.biometricsandroid.TOKEN
import com.example.biometricsandroid.USERNAME
import com.example.biometricsandroid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        login()
    }

    private fun login() {
        binding.loginBtn.setOnClickListener {
            /**
             * Normally this method would asynchronously send this to your server and your sever
             * would return a token.
             * In this sample, we don't call a server.
             * Instead we use a fake token that we set right here:
             */

            val username = binding.usernameEditText.text.toString()
            val token = java.util.UUID.randomUUID().toString()

            val sharedPreferences = getSharedPreferences(SHARED_PREFS_FILENAME, MODE_PRIVATE)
            sharedPreferences.edit()
                .putString(USERNAME, username)
                .putString(TOKEN, token)
                .apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}