package com.example.musicapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.components_activity.*

class ComponentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.components_activity)

        /*val email = FirebaseAuth.getInstance().currentUser?.email
        email_view.text = "Logged in as\n$email"*/

        val intent = Intent(this, ComponentsList::class.java)

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
    }
}