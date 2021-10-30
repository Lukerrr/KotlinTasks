package com.example.task1

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.*
import java.io.*
import java.net.URL
import java.util.*
import javax.swing.JFileChooser
import kotlin.system.exitProcess


class AppController : Initializable {
    //~Begin Scene elements
    @FXML
    private lateinit var delayCurLabel: Label
    @FXML
    private lateinit var timeDelaySelector: Slider
    @FXML
    private lateinit var hideAppCheck: CheckBox
    @FXML
    private lateinit var brushSizeSpinBox: Spinner<Int>
    @FXML
    private lateinit var brushColorPicker: ColorPicker
    @FXML
    private lateinit var canvasPane: AnchorPane
    @FXML
    private lateinit var imageCanvas: Canvas
    @FXML
    private lateinit var drawCanvas: Canvas
    //~End Scene elements

    private lateinit var cropComponent: CropComponent

    private var imageManager: ImageManager = ImageManager()

    private val settingsFileName: String = "app.txt"

    @FXML
    private fun onKeyPressed(e: KeyEvent) {
        /** Handle key shortcut events */
        when (e.code) {
            KeyCode.S -> {
                // Screen capture shortcut (Shift + S)
                if (e.isShiftDown && !e.isAltDown && !e.isControlDown) {
                    requestCaptureImage()
                }

                // SaveAs shortcut (Ctrl + S)
                if (!e.isShiftDown && !e.isAltDown && e.isControlDown) {
                    requestSaveImage(false)
                }

                // Save shortcut (Ctrl + Alt + S)
                if (!e.isShiftDown && e.isAltDown && e.isControlDown) {
                    requestSaveImage(true)
                }
            }

            KeyCode.A -> {
                // Reset crop shortcut (Ctrl + A)
                if (!e.isShiftDown && !e.isAltDown && e.isControlDown) {
                    cropComponent.resetCrop()
                }
            }
        }
    }

    @FXML
    private fun onMouseDraggedDrawCanvas(event: MouseEvent) {
        if (event.isControlDown || event.isShiftDown || event.isAltDown) {
            // Ignore if special keys are down
            return
        }

        if (imageManager.hasImage()) {

            /** Draw circles on canvas */

            val drawSize = brushSizeSpinBox.value.toDouble()
            val drawX = event.x - drawSize / 2.0
            val drawY = event.y - drawSize / 2.0

            val drawCanvasGraphics = drawCanvas.graphicsContext2D

            if (event.button == MouseButton.SECONDARY) {
                drawCanvasGraphics.clearRect(drawX, drawY, drawSize, drawSize)
            } else {
                drawCanvasGraphics.fill = brushColorPicker.value
                drawCanvasGraphics.fillOval(drawX, drawY, drawSize, drawSize)
            }
        }
    }

    @FXML
    private fun onCaptureButtonClicked() {
        requestCaptureImage()
    }

    @FXML
    private fun onOpenMenuClicked() {
        val openImageFile = getFileChooser("*.png", "*.jpg", "*.jpeg").showOpenDialog(imageCanvas.scene.window)
        if (openImageFile != null) {
            imageManager.openImage(openImageFile.toURI().toString())
        }
    }

    @FXML
    private fun onSaveMenuClicked() {
        requestSaveImage(true)
    }

    @FXML
    private fun onSaveAsMenuClicked() {
        requestSaveImage(false)
    }

    @FXML
    private fun onExitMenuClicked() {
        exitProcess(-1)
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        cropComponent = CropComponent(canvasPane)

        /** Bind a delay label to the selector value */
        delayCurLabel.textProperty().bind(
            Bindings.format(
                "%.2f", timeDelaySelector.valueProperty()
            )
        )

        /** Bind to the screen capturer */
        imageManager.bindOnImageUpdatedCallback(::onImageUpdated)

        /** Set values to brush size spinner (min = 1, max = 128, default = 8) */
        brushSizeSpinBox.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 128, 8)
    }

    private fun requestCaptureImage() {
        /** Do screen capture with the delay */
        GlobalScope.launch {
            val delayMs: Double = (timeDelaySelector.value * 1000.0)
            delay(delayMs.toLong())

            // Sync with the main thread
            Platform.runLater {
                val stage: Stage = imageCanvas.scene.window as Stage
                val bHideApp = hideAppCheck.isSelected
                val opacity: Double = stage.opacity

                /** Hide application if necessary */
                if (bHideApp) {
                    stage.opacity = 0.0
                }

                /** Capture screen */
                imageManager.captureScreen()

                /** Unhide application if was hidden */
                if (bHideApp) {
                    stage.opacity = opacity
                }
            }
        }.start()
    }

    private fun requestSaveImage(fastSave: Boolean) {
        if (imageManager.hasImage()) {

            var saveImageFile = if (!fastSave) {
                // Get file from save dialog
                val saveFileChooser = getFileChooser("*.jpg")
                val savePathFile = File(getSavePath())
                if (savePathFile.exists()) {
                    saveFileChooser.initialDirectory = savePathFile
                }
                val choosenFile = saveFileChooser.showSaveDialog(imageCanvas.scene.window)

                if (choosenFile == null) {
                    return
                }

                setSavePath(choosenFile.path.substringBeforeLast("\\", ""))

                choosenFile
            } else {
                // Save file to documents
                val fileName = "\\img" + imageManager.getImageId() + ".jpg"
                File(JFileChooser().fileSystemView.defaultDirectory.toString() + fileName)
            }

            /** Construct an output image */
            val snapshotParams = SnapshotParameters()
            snapshotParams.viewport = cropComponent.toRect()
            val outImage = WritableImage(snapshotParams.viewport.width.toInt(), snapshotParams.viewport.height.toInt())
            canvasPane.snapshot(snapshotParams, outImage)

            /** Save output image */
            imageManager.saveImage(outImage, saveImageFile)
        }
    }

    private fun getFileChooser(vararg extentions: String): FileChooser {
        val fileChooser = FileChooser()
        fileChooser.title = "Open Image"
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Image Files", *extentions))
        return fileChooser
    }

    private fun getSavePath(): String {
        var path = ""
        if (File(settingsFileName).exists()) {
            try {
                val reader = BufferedReader(FileReader(settingsFileName))
                path = reader.readLine()
                reader.close()
            } catch (ex: FileNotFoundException) {
                ex.printStackTrace()
            }
        }
        return path
    }

    private fun setSavePath(path: String) {
        try {
            val writer = PrintWriter(settingsFileName)
            writer.println(path)
            writer.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun onImageUpdated(img: Image) {
        /** Clear draw and image canvas */
        imageCanvas.graphicsContext2D.clearRect(0.0, 0.0, imageCanvas.width, imageCanvas.height);
        drawCanvas.graphicsContext2D.clearRect(0.0, 0.0, drawCanvas.width, drawCanvas.height);

        /** Rescale canvas */
        imageCanvas.width = img.width
        imageCanvas.height = img.height
        drawCanvas.width = img.width
        drawCanvas.height = img.height

        /** Draw screenshot to canvas */
        imageCanvas.graphicsContext2D.drawImage(img, 0.0, 0.0)

        /** Reset crop component */
        cropComponent.isActive = true
        cropComponent.resetCrop()
    }
}