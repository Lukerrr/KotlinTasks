package com.example.task1

import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Rectangle2D
import javafx.scene.image.Image
import javafx.scene.robot.Robot
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO


class ImageManager {

    /** Called when a stored image was updated */
    private var onImageUpdated: ((Image)->Unit)? = null

    /** A currently stored image */
    private var image: Image? = null

    /** An image counter */
    private var imageId: Int = 0

    public fun captureScreen() {
        try {
            val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
            val captureRect = Rectangle2D(0.0, 0.0, screenSize.getWidth(), screenSize.getHeight())

            /** Capture screen */
            updateImage(Robot().getScreenCapture(null, captureRect))
        } catch (ex: IOException) {
            println(ex)
        }
    }

    private fun updateImage(img: Image) {
        if (img != null) {
            image = img
            ++imageId
            onImageUpdated?.invoke(image!!)
        }
    }

    public fun openImage(imagePath: String) {
        updateImage(Image(imagePath))
    }

    public fun saveImage(image: Image, output: File) {
        val ext = output.path.substringAfterLast('.', "")
        if (ext.isNotEmpty()) {
            val bufferedImage = BufferedImage(image.width.toInt(), image.height.toInt(), BufferedImage.TYPE_INT_RGB)
            SwingFXUtils.fromFXImage(image, bufferedImage)
            ImageIO.write(bufferedImage, ext, output)
        }
    }

    public fun getImageId(): Int {
        return imageId
    }

    public fun hasImage() : Boolean {
        return image != null
    }

    public fun bindOnImageUpdatedCallback(inCb: (Image)->Unit) {
        onImageUpdated = inCb
    }
}
