package com.example.musicapp

import java.io.Serializable

class Exercise : Serializable {
    var name: String = ""
    var content: String = ""
    var note: String = ""
    var direction: String = "ascending"
    var highScore: Int = 0
    var numberOfQuestions: Int = 0

    constructor()

    constructor(name: String, content: String, numberOfQuestions: Int, note: String, direction: String) {
        this.name = name
        this.content = content
        this.highScore = 0
        this.direction = direction
        this.note = note
        this.numberOfQuestions = numberOfQuestions
    }
}