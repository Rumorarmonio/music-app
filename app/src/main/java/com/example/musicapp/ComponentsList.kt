package com.example.musicapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.toLowerCase
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.components_list.list
import kotlinx.android.synthetic.main.components_list.to_editor_button
import java.util.*
import kotlin.reflect.KProperty1

class ComponentsList : AppCompatActivity() {

    private lateinit var adapter: FirebaseListAdapter<Component>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.components_list)

        val type = intent.getStringExtra("type").toString()

        val databaseReference = FirebaseDatabase.getInstance().getReference(type)

        to_editor_button.setOnClickListener {
            startActivity(Intent(this, ComponentEditorActivity::class.java).putExtra("type", type))
            finish()
        }

        displayAllComponents(databaseReference)

        list.setOnItemLongClickListener { _, _, position, _ ->
            val name = getInstanceProperty(adapter.getItem(position), "name").toString()
            AlertDialog.Builder(this).setTitle("Delete").setMessage("Are you sure to delete ${type.lowercase(Locale.getDefault())} \"$name\"?")
                .setPositiveButton("Yes") { _, _ ->
                    databaseReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (dataSnapshot: DataSnapshot in snapshot.children)
                                dataSnapshot.ref.removeValue()
                            Toast.makeText(applicationContext, "${type.lowercase(Locale.getDefault())} deleted", Toast.LENGTH_LONG).show()
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }.setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.create().show()

            true
        }
    }

    private fun displayAllComponents(reference: DatabaseReference) {
        adapter = object : FirebaseListAdapter<Component>(
            FirebaseListOptions.Builder<Component>().setLayout(R.layout.component_item).setQuery(reference, Component::class.java).setLifecycleOwner(this).build()
        ) {
            override fun populateView(v: View, model: Component, i: Int) {
                (v.findViewById(R.id.name) as TextView).text = model.name
                (v.findViewById(R.id.content) as TextView).text = model.content
                (v.findViewById(R.id.percent) as TextView).text = if (model.percentage == 0) " — " else "${model.percentage}%"
            }
        }

        val listOfComponents: ListView = findViewById(R.id.list)
        listOfComponents.adapter = adapter
    }

    @Suppress("UNCHECKED_CAST")
    private fun getInstanceProperty(instance: Any, name: String): Any = (instance::class.members.first { it.name == name } as KProperty1<Any, *>).get(instance) as Any
}