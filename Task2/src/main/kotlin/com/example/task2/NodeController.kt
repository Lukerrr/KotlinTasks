package com.example.task2

import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

open class NodeController {
    @FXML
    public lateinit var rootPane: AnchorPane

    public var resource: String = ""
    public var onDestroyCb: (()->Unit)? = null

    @FXML
    private fun onButtonDeletePressed() {
        destroyNode()
    }

    fun getParentPane(): Pane {
        return (rootPane.parent as Pane)
    }

    fun enableDrag() {
        DynamicSceneSystem.makeDraggableNode(rootPane)
    }

    fun disableDrag() {
        DynamicSceneSystem.removeDraggableNode(rootPane)
    }

    fun destroyNode() {
        LinkSystem.unregisterNode(this)
        getParentPane().children.remove(rootPane)
        onDestroyCb?.invoke()
    }

    open fun initNode() {
        enableDrag()
        LinkSystem.registerNode(this)
    }

    open fun getLinks(): Array<LinkDef> {
        return arrayOf()
    }

    open fun updateState() {
        /** Update child nodes */
        for (link in getLinks()) {
            if (link.type == LinkType.OUT) {
                LinkSystem.findLinkage(link)?.node?.updateState()
            }
        }
    }

    open fun serialize(stream: ObjectOutputStream) {
        stream.writeDouble(rootPane.translateX)
        stream.writeDouble(rootPane.translateY)
    }

    open fun deserialize(stream: ObjectInputStream) {
        rootPane.translateX = stream.readDouble()
        rootPane.translateY = stream.readDouble()
    }
}