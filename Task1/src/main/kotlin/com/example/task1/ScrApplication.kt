package com.example.task1

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class ScrApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(ScrApplication::class.java.getResource("scr-app-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 800.0, 600.0)
        stage.title = "Screenshot app"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(ScrApplication::class.java)
}