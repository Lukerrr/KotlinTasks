package com.example.task2

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.shape.Shape
import javafx.stage.Stage


class OutputNodeController: NodeController() {
    @FXML
    private lateinit var imageViewSource: ImageView

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var buttonPreview: Button

    private lateinit var linkDefInImage: LinkDef

    @FXML
    fun onButtonPreviewClicked() {
        val root = FXMLLoader(EditorApplication::class.java.getResource("result-image-view.fxml"))
        val stage = Stage()
        stage.title = "Result Preview"
        stage.scene = Scene(root.load(), 450.0, 450.0)
        root.getController<ResultImageViewController>().setImage(imageViewSource.image)
        stage.show()
    }

    fun getImage() : Image? {
        return imageViewSource.image
    }

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)

        if (inImageLink == null) {
            imageViewSource.image = null
            buttonPreview.isDisable = true
            return
        }

        imageViewSource.image = (inImageLink!!.node as ImageNodeController).getImage()

        buttonPreview.isDisable = imageViewSource.image == null

        super.updateState()
    }
}