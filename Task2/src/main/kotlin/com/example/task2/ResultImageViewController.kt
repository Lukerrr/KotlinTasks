package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.image.ImageView

class ResultImageViewController {
    @FXML
    private lateinit var resultImageView: ImageView

    fun setImage(image: Image) {
        resultImageView.image = image
    }
}