package com.example.fingerprintnotepad

import android.content.Context
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isAuthenticated = intent.getBooleanExtra("isAuthenticated", false)

        if(isAuthenticated)
        {
            loadData()
        }


        SaveButton.setOnClickListener()
        {
            saveData()
        }

        ChangePasswordButton.setOnClickListener()
        {
            saveData()
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
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
            val fos = FileOutputStream(file)
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
            val decrypted = cryptoManager.decrypt(
                inputStream = FileInputStream(file)
            ).decodeToString()

            NoteText.setText(decrypted)
        }
    }
}