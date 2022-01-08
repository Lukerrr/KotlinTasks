package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.shape.Shape

class TransformScaleNodeController: ImageNodeController() {

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var inLinkX: Shape

    @FXML
    private lateinit var inLinkY: Shape

    private lateinit var linkDefInImage: LinkDef
    private lateinit var linkDefInX: LinkDef
    private lateinit var linkDefInY: LinkDef

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        linkDefInX = LinkDef(this, inLinkX, "Input Scale X", LinkType.IN, LinkValueType.FLOAT)
        linkDefInY = LinkDef(this, inLinkY, "Input Scale Y", LinkType.IN, LinkValueType.FLOAT)

        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
            linkDefInX,
            linkDefInY,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)
        val inXLink: LinkDef? = LinkSystem.findLinkage(linkDefInX)
        val inYLink: LinkDef? = LinkSystem.findLinkage(linkDefInY)

        if (inImageLink == null || inXLink == null || inYLink == null) {
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        val inX: Float = (inXLink!!.node as FloatNodeController).getFloat()
        val inY: Float = (inYLink!!.node as FloatNodeController).getFloat()

        if (inX <= 0 || inY <= 0) {
            invalidateImage()
            return
        }

        imageViewSource.image = ImageUtils.transformScale(inImage, inX.toDouble(), inY.toDouble())

        super.updateState()
    }
}