package com.example.musicapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        auth = FirebaseAuth.getInstance()

        sign_up.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        forgot_password.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Type your email here")
            val view = layoutInflater.inflate(R.layout.reset_password_dialog, null)
            builder.setView(view)
            builder.setPositiveButton("Reset") { _, _ ->
                val email = view.findViewById<EditText>(R.id.email_field)
                if (email.text.toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches())
                    return@setPositiveButton

                auth.sendPasswordResetEmail(email.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Toast.makeText(this, "Email sent.", Toast.LENGTH_LONG).show()
                }
            }
            builder.setNegativeButton("close") { _, _ -> }
            builder.show()
        }

        login.setOnClickListener {
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

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                    updateUI(auth.currentUser)
                else
                    updateUI(null)
            }
        }
    }

    @SuppressLint("ShowToast")
    override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null)
            if (currentUser.isEmailVerified) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else
                Toast.makeText(baseContext, "Please verify your email address before login.", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(baseContext, "Login failed.", Toast.LENGTH_SHORT).show()
    }
}