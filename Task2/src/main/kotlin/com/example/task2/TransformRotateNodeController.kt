package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.shape.Shape


class TransformRotateNodeController: ImageNodeController() {

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var inLinkRadians: Shape

    private lateinit var linkDefInImage: LinkDef
    private lateinit var linkDefInRadians: LinkDef

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        linkDefInRadians = LinkDef(this, inLinkRadians, "Input Rotation Radians", LinkType.IN, LinkValueType.FLOAT)

        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
            linkDefInRadians,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)
        val inRadiansLink: LinkDef? = LinkSystem.findLinkage(linkDefInRadians)

        if (inImageLink == null || inRadiansLink == null) {
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        val radians: Float = (inRadiansLink!!.node as FloatNodeController).getFloat()

        imageViewSource.image = ImageUtils.transformRotate(inImage, radians.toDouble())

        super.updateState()
    }
}