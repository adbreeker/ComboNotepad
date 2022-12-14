package com.example.fingerprintnotepad

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity()
{
    private var isAuthenticated : Boolean = false
    private var havePassword : Boolean = false
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        if(!sharedPreferences.contains("password"))
        {
            buttonLogin.setText("Set password")
        }
        else
        {
            havePassword = true
        }

        buttonLogin.setOnClickListener()
        {
            if(editTextTextPassword.text.toString() != "")
            {
                if(havePassword)
                {
                    val keyStretcher = KeyStretcher()
                    val salt  = sharedPreferences.getString("salt",null)
                    val stretchedpass = salt?.let { it1 -> keyStretcher.stretch(editTextTextPassword.text.toString(), it1.encodeToByteArray()) }
                    if(sharedPreferences.getString("password", null) == stretchedpass)
                    {
                        isAuthenticated = true
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("isAuthenticated", isAuthenticated)
                        })
                    }
                    else
                    {
                        Toast.makeText(this, "Wrong password!", Toast.LENGTH_SHORT).show()
                        editTextTextPassword.setText("")
                    }
                }
                else
                {
                    val keyStretcher = KeyStretcher()
                    sharedPreferences.edit().apply()
                    {
                        putString("salt", keyStretcher.salt())
                    }.apply()
                    val salt = sharedPreferences.getString("salt",null)
                    val stretchedpass = salt?.let { it1 -> keyStretcher.stretch(editTextTextPassword.text.toString(), it1.encodeToByteArray()) }
                    sharedPreferences.edit().apply()
                    {
                        putString("password", stretchedpass)
                    }.apply()
                    isAuthenticated = true
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                        putExtra("isAuthenticated", isAuthenticated)
                    })
                }
            }
            else
            {
                Toast.makeText(this, "No password inserted", Toast.LENGTH_SHORT).show()
            }

        }

        buttonFingerprint.setOnClickListener()
        {
            checkDeviceBiometric()
            setupBiometric()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun checkDeviceBiometric()
    {
        val biometricManager = BiometricManager.from(this)
        when(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL))
        {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Toast.makeText(this, "Can authenticate", Toast.LENGTH_LONG).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                Toast.makeText(this, "Can not authenticate", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun setupBiometric()
    {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@LoginActivity, "authentication error: $errString", Toast.LENGTH_LONG).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@LoginActivity, "authenticated, permission granted", Toast.LENGTH_SHORT).show()
                isAuthenticated = true
                startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                    putExtra("isAuthenticated", isAuthenticated)
                })
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LoginActivity, "authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Authentication")
            .setSubtitle("Authenticate to get read/write permission")
            .setNegativeButtonText("Cancel")
            .build()
    }
}