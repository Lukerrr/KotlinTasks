package com.example.task2

import javafx.fxml.FXML

class ImageInputController : ImageSourceNodeController() {
    @FXML
    fun onButtonOpenPressed() {
        val imageFile = ImageUtils.loadImage(imageViewSource.scene.window)

        if (imageFile != null) {
            imageViewSource.image = imageFile
            updateState()
        }
    }
}