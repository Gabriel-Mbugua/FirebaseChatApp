package com.gabriel.firebasemessenger.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gabriel.firebasemessenger.R
import com.gabriel.firebasemessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        login_button_login.setOnClickListener {
            Log.d(TAG, "Attempt to Login")
            signIn()
        }

        back_to_register_login.setOnClickListener {
            finish()
        }
    }

    private fun signIn() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmailAndPassword: success")
                val user = auth.currentUser
                val intent = Intent(this, LatestMessagesActivity::class.java)
                startActivity(intent)
            }
            else{
                Log.d(TAG, "signInWithEmailAndPassword: failure")
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_LONG).show()
            }
        }
            .addOnFailureListener(this){
                Toast.makeText(this, "Authentication failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}