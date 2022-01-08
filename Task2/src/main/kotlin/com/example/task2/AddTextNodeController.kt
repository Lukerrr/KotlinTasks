package com.example.task2

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment

class AddTextNodeController: ImageNodeController() {

    private val FONT_NAME = "Calibri"
    private val FONT_SIZE = 62.0
    private val FONT_COLOR = Color.RED
    private val FONT_ALIGN = TextAlignment.LEFT

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var inLinkX: Shape

    @FXML
    private lateinit var inLinkY: Shape

    @FXML
    private lateinit var inLinkText: Shape

    private lateinit var linkDefInImage: LinkDef
    private lateinit var linkDefInX: LinkDef
    private lateinit var linkDefInY: LinkDef
    private lateinit var linkDefInText: LinkDef

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        linkDefInX = LinkDef(this, inLinkX, "Input Int X", LinkType.IN, LinkValueType.INTEGER)
        linkDefInY = LinkDef(this, inLinkY, "Input Int Y", LinkType.IN, LinkValueType.INTEGER)
        linkDefInText = LinkDef(this, inLinkText, "Input Text String", LinkType.IN, LinkValueType.STRING)

        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
            linkDefInX,
            linkDefInY,
            linkDefInText,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)
        val inXLink: LinkDef? = LinkSystem.findLinkage(linkDefInX)
        val inYLink: LinkDef? = LinkSystem.findLinkage(linkDefInY)
        val inTextLink: LinkDef? = LinkSystem.findLinkage(linkDefInText)

        if (inImageLink == null || inXLink == null || inYLink == null || inTextLink == null) {
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        val inX: Int = (inXLink!!.node as IntegerNodeController).getInteger()
        val inY: Int = (inYLink!!.node as IntegerNodeController).getInteger()
        val inText: String = (inTextLink!!.node as StringNodeController).getString()

        var imageCanvas = Canvas(inImage.width, inImage.height)
        imageCanvas.graphicsContext2D.drawImage(inImage, 0.0, 0.0)
        imageCanvas.graphicsContext2D.fill = FONT_COLOR
        imageCanvas.graphicsContext2D.font =  Font(FONT_NAME, FONT_SIZE)
        imageCanvas.graphicsContext2D.textAlign = FONT_ALIGN
        imageCanvas.graphicsContext2D.fillText(inText, inX.toDouble(), inY.toDouble())

        imageViewSource.image = ImageUtils.snapshotNode(imageCanvas, inImage.width, inImage.height)

        super.updateState()
    }
}