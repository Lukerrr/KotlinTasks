package com.example.task2

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.shape.Shape


class SepiaFilterNodeController: ImageNodeController() {

    @FXML
    private lateinit var inLinkImage: Shape

    private lateinit var linkDefInImage: LinkDef

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
            invalidateImage()
            return
        }

        val inImage: Image? = (inImageLink!!.node as ImageNodeController).getImage()
        if (inImage == null) {
            invalidateImage()
            return
        }

        imageViewSource.image = ImageUtils.filterSepia(inImage)

        super.updateState()
    }
}