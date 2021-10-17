package com.example.musicapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.sherlock.com.sun.media.sound.SF2Soundbank
import cn.sherlock.com.sun.media.sound.SoftSynthesizer
import com.google.firebase.database.*
import jp.kshoji.javax.sound.midi.Receiver
import jp.kshoji.javax.sound.midi.ShortMessage
import kotlinx.android.synthetic.main.exercise_activity.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.random.Random


class ExerciseActivity : AppCompatActivity() {

    private lateinit var receiver: Receiver
    private val lowestNote = 60
    private val highestNote = 84
    private val bpm = 120
    private var questionsLeft: Int = 0
    private var numberOfQuestions: Int = 0
    private var rightAnswer = ""
    private var direction = "ascending"
    private lateinit var exerciseName: String
    private lateinit var playedLast: Map.Entry<String, String>
    private var lastFirstNote: Int = 0
    private var isMixed: Boolean = false
    private val directions = setOf("ascending", "descending", "harmonic")
    private var rightAnswers: Int = 0
    private var wrongAnswers: Int = 0
    private lateinit var databaseReference: DatabaseReference
    private lateinit var componentsDatabaseReference: DatabaseReference
    private var highScore: Int = 0
    private lateinit var type: String

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_activity)

        val extras = intent.extras
        val content = extras!!.getString("content")
        exerciseName = extras.getString("name")!!
        numberOfQuestions = extras.getString("numberOfQuestions")!!.toInt()
        questionsLeft = numberOfQuestions
        val note = extras.getString("note")
        direction = extras.getString("direction")!!
        highScore = extras.getInt("highScore")

        type = "${extras.getString("type")}"
        databaseReference = FirebaseDatabase.getInstance().getReference("${type}Exercise")

        isMixed = direction == "mixed"

        exercise_title.text = exerciseName
        questions_left.text = "$questionsLeft questions left"

        val soundFont2Soundbank = SF2Soundbank(assets.open("SmallTimGM6mb.sf2"))
        val synth = SoftSynthesizer()
        synth.open()
        synth.loadAllInstruments(soundFont2Soundbank)
        synth.channels[0].programChange(0)
        receiver = synth.receiver

        val names = note?.split(", ")?.toTypedArray()
        val contents = content?.split(" + ")?.toTypedArray()

        val map: Map<String, String> = names!!.zip(contents!!).toMap()

        nextQuestion(map)

        for ((counter, it) in names.withIndex()) {
            val componentView = layoutInflater.inflate(R.layout.component_button, null, false)
            val button = (componentView.findViewById<View>(R.id.button) as Button)
            button.text = it
            button.setOnClickListener {
                val text = button.text
                val message: String
                if (text == rightAnswer) {
                    message = "You are right"
                    rightAnswers++
                } else {
                    message = "You are wrong"
                    wrongAnswers++
                }
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                var rightAnswers: Int
                var wrongAnswers: Int
                var flag = true
                componentsDatabaseReference = FirebaseDatabase.getInstance().getReference(type)
                componentsDatabaseReference.orderByChild("name").equalTo("$text").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            rightAnswers = snapshot.child("rightAnswers").value.toString().toInt()
                            wrongAnswers = snapshot.child("wrongAnswers").value.toString().toInt()
                            if (flag)
                                if (text == rightAnswer) {
                                    flag = false
                                    rightAnswers++
                                    snapshot.ref.child("rightAnswers").setValue(rightAnswers)
                                    snapshot.ref.child("percentage")
                                        .setValue(((rightAnswers.toDouble() / (rightAnswers.toDouble() + wrongAnswers.toDouble())) * 100).roundToInt())
                                } else {
                                    flag = false
                                    updateIfWrong()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

                nextQuestion(map)
            }
            if (counter % 2 == 0)
                buttons_layout_1.addView(componentView)
            else
                buttons_layout_2.addView(componentView)
        }

        hint_button.setOnClickListener {
            Toast.makeText(this, rightAnswer, Toast.LENGTH_SHORT).show()
        }

        repeat_button.setOnClickListener {
            playComponent(playedLast.value, lastFirstNote)
        }
    }

    private fun updateIfWrong() {
        var rightAnswers: Int
        var wrongAnswers: Int
        var flag = true
        componentsDatabaseReference.orderByChild("name").equalTo(rightAnswer).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    rightAnswers = snapshot.child("rightAnswers").value.toString().toInt()
                    wrongAnswers = snapshot.child("wrongAnswers").value.toString().toInt()
                    if (flag) {
                        wrongAnswers++
                        snapshot.ref.child("wrongAnswers").setValue(wrongAnswers)
                        flag = false
                    }
                    snapshot.ref.child("percentage")
                        .setValue(((rightAnswers.toDouble() / (rightAnswers.toDouble() + wrongAnswers.toDouble())) * 100).roundToInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun nextQuestion(map: Map<String, String>) {
        if (questionsLeft > 0) {
            if (isMixed) {
                direction = directions.shuffled().first()
                Log.i("mytag", "direction: $direction")
            }

            playedLast = map.entries.shuffled().first()

            lastFirstNote = Random.nextInt(lowestNote, highestNote)
            playComponent(playedLast.value, lastFirstNote)

            rightAnswer = playedLast.key
            Log.i("mytag", "right answer is: $playedLast")
            questionsLeft--

            questions_left.text = "$questionsLeft questions left"
            right_answers.text = "Right answers: $rightAnswers"
            wrong_answers.text = "Wrong answers: $wrongAnswers"
        } else {
            val newHighScore = ((rightAnswers.toDouble() / numberOfQuestions) * 100).roundToInt()
            if (newHighScore > highScore)
                databaseReference.orderByChild("name").equalTo(exerciseName).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot: DataSnapshot in snapshot.children)
                            dataSnapshot.ref.child("highScore").setValue(newHighScore)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            finish()
        }
    }

    private fun playComponent(str: String, firstNote: Int) {
        var intervals = str.split(" ").toTypedArray().map { it.toInt() }
        if (direction == "descending") intervals = intervals.reversed()

        Log.i("mytag", intervals.toString())

        val ms = bpmToMilliseconds(bpm)

        var previousNote = firstNote

        CoroutineScope(Dispatchers.IO).launch {
            if (direction != "harmonic") {
                playNote(previousNote, ms)
                for (it in intervals) {
                    previousNote += if (direction == "descending") -it else it
                    playNote(previousNote, ms)
                }
            } else
                playNotesSimultaneously(previousNote, intervals, ms)
        }
    }

    private fun playNote(note: Int, duration: Int) {
        val msg = ShortMessage()
        msg.setMessage(ShortMessage.NOTE_ON, 0, note, 127)
        Log.i("mytag", msg.toString())
        receiver.send(msg, -1)

        Thread.sleep(duration.toLong())

        msg.setMessage(ShortMessage.NOTE_OFF, 0, note, 127)
        receiver.send(msg, -1)
    }

    private fun playNotesSimultaneously(firstNote: Int, intervals: List<Int>, duration: Int) {
        val msg = ShortMessage()
        var message = ShortMessage.NOTE_ON
        for (i in 0..1) {
            var previousNote = firstNote
            for (it in 0 until intervals.size + 1) {
                msg.setMessage(message, 0, previousNote, 127)
                receiver.send(msg, -1)
                if (it != intervals.size)
                    previousNote += intervals[it]
                Log.i("mytag", msg.toString())
            }
            message = ShortMessage.NOTE_OFF
            Thread.sleep(duration.toLong())
        }
    }

    private fun bpmToMilliseconds(bpm: Int) = 60000 / bpm
}