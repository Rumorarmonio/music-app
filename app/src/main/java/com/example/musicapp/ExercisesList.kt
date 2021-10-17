package com.example.musicapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.exercises_list.*
import kotlin.reflect.KProperty1

class ExercisesList : AppCompatActivity() {

    private lateinit var adapter: FirebaseListAdapter<Exercise>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercises_list)

        val type = intent.getStringExtra("type").toString()

        to_editor_button.setOnClickListener {
            startActivity(Intent(this, ExerciseEditorActivity::class.java).putExtra("type", type))
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("${type}Exercise")
        displayAllExercises(databaseReference)

        list.setOnItemClickListener { _, _, position, _ ->
//            Toast.makeText(this, "Clicked item : $position", Toast.LENGTH_LONG).show()
            val selectedItem: Exercise = adapter.getItem(position)

            val extras = Bundle()
            extras.putString("content", getInstanceProperty(selectedItem, "content").toString())
            extras.putString("name", getInstanceProperty(selectedItem, "name").toString())
            extras.putString("numberOfQuestions", getInstanceProperty(selectedItem, "numberOfQuestions").toString())
            extras.putString("note", getInstanceProperty(selectedItem, "note").toString())
            extras.putString("direction", getInstanceProperty(selectedItem, "direction").toString())
            extras.putInt("highScore", getInstanceProperty(selectedItem, "highScore").toString().toInt())
            extras.putString("type", type).toString()

            this.startActivity(Intent(this, ExerciseActivity::class.java).putExtras(extras))
        }

        list.setOnItemLongClickListener { _, _, position, _ ->
            val name = getInstanceProperty(adapter.getItem(position), "name").toString()
            AlertDialog.Builder(this).setTitle("Delete").setMessage("Are you sure to delete exercise \"$name\"?").setPositiveButton("Yes") { _, _ ->
                databaseReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot: DataSnapshot in snapshot.children)
                            dataSnapshot.ref.removeValue()
                        Toast.makeText(applicationContext, "Exercise deleted", Toast.LENGTH_LONG).show()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create().show()

            true
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getInstanceProperty(instance: Any, name: String): Any = (instance::class.members.first { it.name == name } as KProperty1<Any, *>).get(instance) as Any

    private fun displayAllExercises(reference: DatabaseReference) {
        adapter = object : FirebaseListAdapter<Exercise>(
            FirebaseListOptions.Builder<Exercise>().setLayout(R.layout.exercise_item).setQuery(reference, Exercise::class.java).setLifecycleOwner(this).build()
        ) {
            @SuppressLint("SetTextI18n")
            override fun populateView(v: View, model: Exercise, i: Int) {
                (v.findViewById(R.id.exercise_name) as TextView).text = model.name
                (v.findViewById(R.id.high_score) as TextView).text = if (model.highScore == 0) " — " else "${model.highScore}%"
                (v.findViewById(R.id.exercise_note) as TextView).text = model.note
                (v.findViewById(R.id.exercise_direction) as TextView).text = model.direction
                (v.findViewById(R.id.exercise_number_of_questions) as TextView).text = model.numberOfQuestions.toString()
            }
        }

        val listOfComponents: ListView = findViewById(R.id.list)
        listOfComponents.adapter = adapter
    }
}