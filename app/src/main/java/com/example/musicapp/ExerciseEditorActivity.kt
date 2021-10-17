package com.example.musicapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.exersice_editor_activity.*
import java.util.*
import kotlin.reflect.KProperty1


class ExerciseEditorActivity : AppCompatActivity() {

    private lateinit var layoutList: LinearLayout
    private lateinit var componentAdapter: FirebaseListAdapter<Component>
    private var totalContent = ""
    private var totalNote = ""
    private var componentsQuantity: Int = 0

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exersice_editor_activity)

        val type = intent.getStringExtra("type").toString()
        editor_title.text = "$type exercise editor"
        add_component_button.text = "Add ${type.lowercase(Locale.getDefault())}"
        create_component_button.text = "Create new ${type.lowercase(Locale.getDefault())}"

        componentAdapter = object : FirebaseListAdapter<Component>(
            FirebaseListOptions.Builder<Component>().setLayout(R.layout.component_item).setQuery(FirebaseDatabase.getInstance().getReference(type), Component::class.java)
                .setLifecycleOwner(this).build()
        ) {
            override fun populateView(v: View, model: Component, i: Int) {
                (v.findViewById(R.id.name) as TextView).text = model.name
                (v.findViewById(R.id.content) as TextView).text = model.content
            }
        }

        layoutList = findViewById(R.id.layout_list)
        add_component_button.setOnClickListener {
            val componentView = layoutInflater.inflate(R.layout.row_add_component, null, false)
            (componentView.findViewById<View>(R.id.spinner_component) as Spinner).adapter = componentAdapter

            (componentView.findViewById<View>(R.id.image_remove) as ImageView).setOnClickListener {
                layoutList.removeView(componentView)
            }

            layoutList.addView(componentView)
        }

        create_component_button.setOnClickListener {
            startActivity(Intent(this, ComponentEditorActivity::class.java).putExtra("type", type).putExtra("isFromEditor", true))
        }

        val directionsAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.directions, R.layout.direction_item)
        directionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        directionSpinner.adapter = directionsAdapter

        var direction = "ascending"
        directionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                direction = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val et = findViewById<View>(R.id.name) as EditText
        val exercisesReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("${type}Exercise")
        add_button.setOnClickListener {
            if (checkIfValidAndRead()) {
                val name = et.text.toString()
                exercisesReference.push().setValue(
                    Exercise(
                        name = if (name.isEmpty()) totalNote else name,
                        content = totalContent,
                        note = totalNote,
                        numberOfQuestions = number_of_questions.text.toString().toInt(),
                        direction = direction
                    )
                )
                Toast.makeText(this, "Exercise added", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, ExercisesList::class.java).putExtra("type", type))
                finish()
            } else Toast.makeText(this, "Add ${type.lowercase(Locale.getDefault())}s first!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfValidAndRead(): Boolean {
        componentsQuantity = 0
        var name: String
        var content: String
        for (i in 0 until layoutList.childCount) {
            val instance = ((layoutList.getChildAt(i)).findViewById<View>(R.id.spinner_component) as Spinner).selectedItem
            name = getInstanceProperty(instance, "name").toString()
            content = getInstanceProperty(instance, "content").toString()

            totalContent += if (totalContent == "") content else " + $content"
            totalNote += if (totalNote == "") name else ", $name"
            componentsQuantity++
        }
        if (componentsQuantity == 0) return false
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun getInstanceProperty(instance: Any, name: String): Any = (instance::class.members.first { it.name == name } as KProperty1<Any, *>).get(instance) as Any
}