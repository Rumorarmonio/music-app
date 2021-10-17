package com.example.musicapp

import android.content.Intent
import android.os.Bundle

import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)

        auth = FirebaseAuth.getInstance()

        btn_sign_up.setOnClickListener {
            if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
                email.error = "Email or password are empty"
                email.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                email.error = "Please enter valid email"
                email.requestFocus()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    }
                } else
                    Toast.makeText(baseContext, "Sign Up failed. Try again later.", Toast.LENGTH_LONG).show()
            }
        }
    }
}