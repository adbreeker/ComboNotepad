package com.example.fingerprintnotepad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity()
{
    private var isAuthenticated : Boolean = false
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkDeviceBiometric()
        setupBiometric()

        biometricPrompt.authenticate(promptInfo)


        SaveButton.setOnClickListener()
        {
            saveData()
        }
    }

    private fun checkDeviceBiometric()
    {
        val biometricManager = BiometricManager.from(this)
        when(biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL))
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
                Toast.makeText(this@MainActivity, "authentication error: $errString", Toast.LENGTH_LONG).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@MainActivity, "authenticated, permission granted", Toast.LENGTH_SHORT).show()
                isAuthenticated = true
                loadData()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@MainActivity, "authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = PromptInfo.Builder()
            .setTitle("Fingerprint Authentication")
            .setSubtitle("Authenticate to get read/write permission")
            .setNegativeButtonText("Cancel")
            .build()
    }



    private fun saveData()
    {
        if(isAuthenticated)
        {
            val bytesToEncrypt = NoteText.text.toString().encodeToByteArray()
            val cryptoManager = CryptoManager()
            val file = File(filesDir, "note.txt")
            if(!file.exists())
            {
                file.createNewFile()
            }
            var fos = FileOutputStream(file)
            cryptoManager.encrypt(bytes = bytesToEncrypt, outputStream = fos)

            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(this, "You are not allowed to", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadData()
    {
        val cryptoManager = CryptoManager()
        val file = File(filesDir, "note.txt")
        if(!file.exists())
        {
            file.createNewFile()
            NoteText.setText("")
        }
        else
        {
            var decrypted = cryptoManager.decrypt(
                inputStream = FileInputStream(file)
            ).decodeToString()

            NoteText.setText(decrypted)
        }
    }
}