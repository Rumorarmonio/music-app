package com.example.musicapp

import java.io.Serializable

class Component : Serializable {
    var name: String = ""
    var content: String = ""
    var percentage: Int = 0
    var rightAnswers: Int = 0
    var wrongAnswers: Int = 0

    constructor() {}

    constructor(name: String, content: String) {
        this.name = name
        this.content = content
        this.percentage = 0
        this.rightAnswers = 0
        this.wrongAnswers = 0
    }
}