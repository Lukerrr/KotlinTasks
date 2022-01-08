package com.example.task2

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.shape.Shape

class AddImageNodeController: ImageNodeController() {

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var inLinkX: Shape

    @FXML
    private lateinit var inLinkY: Shape

    @FXML
    private lateinit var inLinkAddImage: Shape

    private lateinit var linkDefInImage: LinkDef
    private lateinit var linkDefInX: LinkDef
    private lateinit var linkDefInY: LinkDef
    private lateinit var linkDefInAddImage: LinkDef

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        linkDefInX = LinkDef(this, inLinkX, "Input Int X", LinkType.IN, LinkValueType.INTEGER)
        linkDefInY = LinkDef(this, inLinkY, "Input Int Y", LinkType.IN, LinkValueType.INTEGER)
        linkDefInAddImage = LinkDef(this, inLinkAddImage, "Input Add Image", LinkType.IN, LinkValueType.IMAGE)

        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
            linkDefInX,
            linkDefInY,
            linkDefInAddImage,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)
        val inXLink: LinkDef? = LinkSystem.findLinkage(linkDefInX)
        val inYLink: LinkDef? = LinkSystem.findLinkage(linkDefInY)
        val inAddImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInAddImage)

        if (inImageLink == null || inXLink == null || inYLink == null || inAddImageLink == null) {
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        val inAddImage: Image? = (inAddImageLink!!.node as ImageNodeController).getImage()
        if (inAddImage == null) {
            invalidateImage()
            return
        }

        val inX: Int = (inXLink!!.node as IntegerNodeController).getInteger()
        val inY: Int = (inYLink!!.node as IntegerNodeController).getInteger()

        var imageCanvas = Canvas(inImage.width, inImage.height)
        imageCanvas.graphicsContext2D.drawImage(inImage, 0.0, 0.0)
        imageCanvas.graphicsContext2D.drawImage(inAddImage, inX.toDouble(), inY.toDouble())

        imageViewSource.image = ImageUtils.snapshotNode(imageCanvas, inImage.width, inImage.height)

        super.updateState()
    }
}