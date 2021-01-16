package com.example.biometricsandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * FROM:
 * 1) https://developer.android.com/codelabs/biometric-login#0
 * 2) https://github.com/googlecodelabs/biometric-login
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}