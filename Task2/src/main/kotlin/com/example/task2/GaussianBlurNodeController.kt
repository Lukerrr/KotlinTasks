package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.shape.Shape


class GaussianBlurNodeController: ImageNodeController() {

    @FXML
    private lateinit var inLinkImage: Shape

    @FXML
    private lateinit var inLinkSize: Shape

    private lateinit var linkDefInImage: LinkDef
    private lateinit var linkDefInSize: LinkDef

    override fun initNode() {
        linkDefInImage = LinkDef(this, inLinkImage, "Input Image", LinkType.IN, LinkValueType.IMAGE)
        linkDefInSize = LinkDef(this, inLinkSize, "Input Blur Size", LinkType.IN, LinkValueType.INTEGER)

        super.initNode()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefInImage,
            linkDefInSize,
        )
    }

    override fun updateState() {
        val inImageLink: LinkDef? = LinkSystem.findLinkage(linkDefInImage)
        val inSizeLink: LinkDef? = LinkSystem.findLinkage(linkDefInSize)

        if (inImageLink == null || inSizeLink == null) {
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        val size: Int = (inSizeLink!!.node as IntegerNodeController).getInteger()

        imageViewSource.image = ImageUtils.filterGaussianBlur(inImage, size.toDouble())

        super.updateState()
    }
}