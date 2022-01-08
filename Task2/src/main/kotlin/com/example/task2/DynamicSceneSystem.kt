package com.example.task2

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane

class DynamicSceneSystem {

    companion object {

        private var scene: Scene? = null

        private var mouseAnchorX: Double = 0.0
        private var mouseAnchorY: Double = 0.0
        private var paneAnchorX: Double = 0.0
        private var paneAnchorY: Double = 0.0

        private var mouseDragX: Double = 0.0
        private var mouseDragY: Double = 0.0
        private var nodeAnchorX: Double = 0.0
        private var nodeAnchorY: Double = 0.0

        fun initializeScene(scene: Scene) {
            this.scene = scene
        }

        fun makeDraggableNode(node: Node) {

            node.onMousePressed = EventHandler { e ->
                run {
                    if (!e.isSecondaryButtonDown)
                    {
                        mouseDragX = e.sceneX
                        mouseDragY = e.sceneY
                        nodeAnchorX = node.translateX
                        nodeAnchorY = node.translateY
                    }
                }
            }

            node.onMouseDragged = EventHandler { e ->
                run {
                    if (!e.isSecondaryButtonDown)
                    {
                        node.translateX = nodeAnchorX + (e.sceneX - mouseDragX)
                        node.translateY = nodeAnchorY + (e.sceneY - mouseDragY)
                    }
                }
            }

        }

        fun removeDraggableNode(node: Node) {
            node.onMousePressed = EventHandler {}
            node.onMouseDragged = EventHandler {}
        }

        fun makeDynamicPane(pane: Pane)
        {
            scene?.onMousePressed = EventHandler { e ->
                run {
                    if (e.button == MouseButton.SECONDARY)
                    {
                        mouseAnchorX = e.sceneX
                        mouseAnchorY = e.sceneY
                        paneAnchorX = pane.translateX
                        paneAnchorY = pane.translateY
                    }
                }
            }

            scene?.onMouseDragged = EventHandler { e ->
                run {
                    if (e.button == MouseButton.SECONDARY)
                    {
                        pane.translateX = paneAnchorX + e.sceneX - mouseAnchorX
                        pane.translateY = paneAnchorY + e.sceneY - mouseAnchorY
                    }
                }
            }
        }
    }
}