package com.example.task2

import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.effect.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Window
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class ImageUtils {

    companion object {

        fun loadImage(window: Window): Image? {
            val openImageFile = FileUtils
                .getFileChooser("Open Image", "Image Files", "*.png", "*.jpg", "*.jpeg")
                .showOpenDialog(window)

            if (openImageFile == null) {
                return null
            }

            return Image(openImageFile.toURI().toString())
        }

        fun saveImage(window: Window, image: Image) {
            val choosenFile = FileUtils
                .getFileChooser("Save Image", "Image Files", "*.jpg")
                .showSaveDialog(window)

            if (choosenFile == null) {
                return
            }

            val ext = choosenFile.path.substringAfterLast('.', "")
            if (ext.isNotEmpty()) {
                val bufferedImage = BufferedImage(image.width.toInt(), image.height.toInt(), BufferedImage.TYPE_INT_RGB)
                SwingFXUtils.fromFXImage(image, bufferedImage)
                ImageIO.write(bufferedImage, ext, choosenFile)
            }
        }

        fun filterGrayScale(image: Image): Image {
            val grayFilter = ColorAdjust()
            grayFilter.saturation = -1.0
            val imageViewGray = ImageView(image)
            imageViewGray.effect = grayFilter

            return snapshotNode(imageViewGray, image.width, image.height)
        }

        fun filterSepia(image: Image): Image {
            val sepiaTone = SepiaTone()
            sepiaTone.level = 0.7

            val imageViewSepia = ImageView(image)
            imageViewSepia.effect = sepiaTone

            return snapshotNode(imageViewSepia, image.width, image.height)
        }

        fun filterInvert(image: Image): Image {
            val color = ColorInput()
            color.paint = Color.WHITE
            color.width = Double.MAX_VALUE
            color.height = Double.MAX_VALUE

            val invert = Blend(BlendMode.DIFFERENCE)
            invert.bottomInput = color;

            val imageViewInvert = ImageView(image)
            imageViewInvert.effect = invert

            return snapshotNode(imageViewInvert, image.width, image.height)
        }

        fun filterGaussianBlur(image: Image, size: Double): Image {
            val color = ColorInput()
            color.paint = Color.WHITE
            color.width = Double.MAX_VALUE
            color.height = Double.MAX_VALUE

            val blur = GaussianBlur()
            blur.radius = size;

            val imageViewBlur = ImageView(image)
            imageViewBlur.effect = blur

            return snapshotNode(imageViewBlur, image.width, image.height)
        }

        fun filterBrightnessAdjust(image: Image, bright: Double): Image {
            val brightFilter = ColorAdjust()
            brightFilter.brightness = bright
            val imageViewBright = ImageView(image)
            imageViewBright.effect = brightFilter

            return snapshotNode(imageViewBright, image.width, image.height)
        }

        fun transformMove(image: Image, x: Double, y: Double): Image {
            val moveEffect = DisplacementMap()
            moveEffect.offsetX = x
            moveEffect.offsetY = y
            val imageViewMove = ImageView(image)
            imageViewMove.effect = moveEffect

            return snapshotNode(imageViewMove, image.width, image.height)
        }

        fun transformScale(image: Image, x: Double, y: Double): Image {
            val imageViewScale = ImageView(image)

            imageViewScale.fitWidth = image.width * x
            imageViewScale.fitHeight = image.height * y

            return snapshotNode(imageViewScale, imageViewScale.fitWidth, imageViewScale.fitHeight)
        }

        fun transformRotate(image: Image, angle: Double): Image {
            val imageViewRotate = ImageView(image)
            imageViewRotate.rotate = Math.toDegrees(angle)

            val rotatedBounds1 = Rectangle2D(
                imageViewRotate.boundsInParent.minX, imageViewRotate.boundsInParent.minY,
                imageViewRotate.boundsInParent.width, imageViewRotate.boundsInParent.height
            )

            return snapshotNodeRect(imageViewRotate, rotatedBounds1)
        }

        fun snapshotNode(node: Node, width: Double, height: Double): Image {
            return snapshotNodeRect(node, Rectangle2D(0.0, 0.0, width, height))
        }

        fun snapshotNodeRect(node: Node, rect: Rectangle2D): Image {
            val snapshotParams = SnapshotParameters()
            snapshotParams.viewport = rect

            val outImage = WritableImage(snapshotParams.viewport.width.toInt(), snapshotParams.viewport.height.toInt())
            node.snapshot(snapshotParams, outImage)

            return outImage
        }

    }
}