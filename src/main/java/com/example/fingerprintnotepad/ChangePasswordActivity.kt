package com.example.fingerprintnotepad

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_login.*

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val keyStretcher = KeyStretcher()
        if(!sharedPreferences.contains("salt"))
        {
            sharedPreferences.edit().apply()
            {
                putString("salt", keyStretcher.salt())
            }.apply()
        }
        val salt = sharedPreferences.getString("salt", null)

        ChangePasswordSetButton.setOnClickListener()
        {
            if(editTextTextPassword2.text.toString() == "")
            {
                Toast.makeText(this, "No password inserted", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val newstretchedpass = salt?.let { it1 -> keyStretcher.stretch(editTextTextPassword2.text.toString(), it1.encodeToByteArray()) }
                sharedPreferences.edit().apply()
                {
                    putString("password", newstretchedpass)
                }.apply()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("isAuthenticated", true)
                })
            }
        }
    }
}