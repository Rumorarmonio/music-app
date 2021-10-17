package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.activity_main.email_view

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val email = FirebaseAuth.getInstance().currentUser?.email
        email_view.text = "Logged in as\n$email"*/

        exercises.setOnClickListener {
            startActivity(Intent(this, ExercisesActivity::class.java))
        }

        lists.setOnClickListener {
            startActivity(Intent(this, ComponentsActivity::class.java))
        }
    }
}