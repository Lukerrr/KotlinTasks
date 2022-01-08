package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.shape.Shape

open class ImageNodeController : NodeController() {
    @FXML
    public lateinit var imageViewSource: ImageView

    @FXML
    private lateinit var outLinkImage: Shape

    private lateinit var linkDefOutImage: LinkDef

    override fun initNode() {
        linkDefOutImage = LinkDef(this, outLinkImage, "Output Image", LinkType.OUT, LinkValueType.IMAGE)

        super.initNode()
    }

    protected fun invalidateImage() {
        imageViewSource.image = null
        super.updateState()
    }

    open fun getImage() : Image? {
        return imageViewSource.image
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefOutImage,
        )
    }
}