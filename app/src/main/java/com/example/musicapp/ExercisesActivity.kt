package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
//import kotlinx.android.synthetic.main.activity_main.email_view
import kotlinx.android.synthetic.main.exercises_activity.*

class ExercisesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercises_activity)

        val intent = Intent(this, ExercisesList::class.java)

        intervals_button.setOnClickListener {
            intent.putExtra("type", "Interval")
            startActivity(intent)
        }

        chords_button.setOnClickListener {
            intent.putExtra("type", "Chord")
            startActivity(intent)
        }

        scales_button.setOnClickListener {
            intent.putExtra("type", "Scale")
            startActivity(intent)
        }

        /*val email = FirebaseAuth.getInstance().currentUser?.email
        email_view.text = "Logged in as\n$email"*/
    }
}