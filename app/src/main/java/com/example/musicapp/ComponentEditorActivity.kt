package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.component_editor_activity.*

class ComponentEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.component_editor_activity)

        val type = intent.getStringExtra("type").toString()
        val isFromEditor = intent.getBooleanExtra("isFromEditor", false)

        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference(type)

        add_button.setOnClickListener {
            reference.push().setValue(Component(/*type = reference.key.toString(), */name = name.text.toString(), content = content.text.toString()))
            Toast.makeText(this, "$type added", Toast.LENGTH_SHORT).show()
            if (!isFromEditor)
                startActivity(Intent(this, ComponentsList::class.java).putExtra("type", type))
            finish()
        }
    }
}