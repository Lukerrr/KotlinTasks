package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.shape.Shape


class BrightnessNodeController: ImageNodeController() {

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var inLinkBright: Shape

    private lateinit var linkDefInImage: LinkDef
    private lateinit var linkDefInBright: LinkDef

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        linkDefInBright = LinkDef(this, inLinkBright, "Input Bright Adjust", LinkType.IN, LinkValueType.FLOAT)

        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
            linkDefInBright,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)
        val inBrightLink: LinkDef? = LinkSystem.findLinkage(linkDefInBright)

        if (inImageLink == null || inBrightLink == null) {
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        val bright: Float = (inBrightLink!!.node as FloatNodeController).getFloat()

        imageViewSource.image = ImageUtils.filterBrightnessAdjust(inImage, bright.toDouble())

        super.updateState()
    }
}