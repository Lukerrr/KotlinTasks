package com.example.task2

import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.CubicCurve
import javafx.scene.shape.Shape
import javafx.util.Duration

/** Link values enumeration with colors */
enum class LinkValueType(val color: Paint) {
    FLOAT       (Color.RED),
    INTEGER     (Color.ORANGE),
    STRING      (Color.GREEN),
    IMAGE       (Color.BLUE),
}

/** Link connection types */
enum class LinkType {
    IN,
    OUT,
}

class LinkDef {

    // A node to which the link belongs
    public var node: NodeController

    // A shape object in the scene, associated with the link
    public var shape: Shape

    // A human-readable name of the link
    public var name: String

    // I/O type
    public var type: LinkType

    // Type of the value
    public var valueType: LinkValueType

    constructor(node: NodeController, shape: Shape, name: String, type: LinkType, valueType: LinkValueType) {
        this.node = node
        this.shape = shape
        this.name = name
        this.type = type
        this.valueType = valueType
    }

}

class LinkSystem {
    
    companion object {

        // A scaling for link shape when it is hovered/dragged
        private const val LINK_HOVER_SCALE = 1.5

        // A width for linkage curves
        private const val LINKAGE_STROKE_WIDTH = 1.75

        // Current linkage source
        private var linkageCurrentLinkSource: LinkDef? = null

        // Current linkage  target
        private var linkageCurrentLinkTarget: LinkDef? = null

        // Current linkage curve, guided by a user
        private var linkageCurrentCurve: CubicCurve? = null

        /**
         * Stores a collection of all current links.
         * Each entry contains a source link associated with a pair
         * of a target link and a linkage curve object.
         *
         * Each linkage has 2 entries with equal curves, that helps in 2-sided navigation:
         *  1) link1 -> link2
         *  2) link2 -> link1
         *
         * link1 is always OUT
         */
        private var linksMap: MutableMap<LinkDef, Pair<LinkageCurve, LinkDef>> = mutableMapOf()

        public fun registerNode(node: NodeController) {
            for (nodeLink in node.getLinks()) {
                val linkShape: Shape = nodeLink.shape

                // Set color associated with the value type
                linkShape.fill = nodeLink.valueType.color

                // Set tooltip
                val linkHint = Tooltip(nodeLink.name)
                linkHint.showDelay = Duration(100.0)
                linkHint.hideDelay = Duration(0.0)
                Tooltip.install(linkShape, linkHint)

                linkShape.onDragDetected = EventHandler { run {
                    // Enable global dragging events
                    linkShape.startFullDrag()
                } }

                linkShape.onMouseDragEntered = EventHandler { run {
                    if (areLinkable(linkageCurrentLinkSource!!, nodeLink)) {
                        // Highlight link when mouse is dragging over and we can connect to this link
                        highlightLink(nodeLink)
                        linkageCurrentLinkTarget = nodeLink

                    }
                } }

                linkShape.onMouseDragExited = EventHandler { run {
                    if (nodeLink == linkageCurrentLinkTarget) {
                        // Reset link when mouse drag is left and the link is not source
                        unhighlightLink(nodeLink)
                        linkageCurrentLinkTarget = null
                    }
                } }

                linkShape.onMouseEntered = EventHandler { run {
                    // Highlight when mouse enters link
                    highlightLink(nodeLink)
                } }

                linkShape.onMouseExited = EventHandler { run {
                    /** Reset when mouse leaves link and link is not dragged */
                    if (!linkShape.isPressed) {
                        unhighlightLink(nodeLink)
                    }
                } }

                /** Implement link dragging and connection with other links */

                /** Mouse drag - Move linkage curve */
                linkShape.onMouseDragged = EventHandler { e -> run {

                    if (e.button != MouseButton.PRIMARY) {
                        return@run
                    }

                    var startPt = getLinkPos(linkageCurrentLinkSource!!)
                    var endPt = Point2D(e.sceneX - node.getParentPane().layoutX - node.getParentPane().translateX,
                        e.sceneY - node.getParentPane().layoutY - node.getParentPane().translateY)

                    if (linkageCurrentLinkSource!!.type == LinkType.IN) {
                        startPt = endPt.also { endPt = startPt }
                    }

                    LinkageCurve.setupCubicCurve(linkageCurrentCurve!!, startPt, endPt)

                } }

                /** LMB press - Enable linkage drag */
                linkShape.onMousePressed = EventHandler { e -> run {

                    if (e.button != MouseButton.PRIMARY) {
                        return@run
                    }

                    // Highlight when mouse presses link
                    highlightLink(nodeLink)

                    // Disable node dragging when a link is used
                    node.disableDrag()

                    // Create a preview curve for linkage
                    linkageCurrentLinkSource = nodeLink
                    linkageCurrentCurve = CubicCurve()
                    linkageCurrentCurve?.isMouseTransparent = true
                    linkageCurrentCurve?.fill = Color.TRANSPARENT
                    linkageCurrentCurve?.stroke = nodeLink.valueType.color
                    linkageCurrentCurve?.strokeWidth = LINKAGE_STROKE_WIDTH
                    linkageCurrentCurve?.viewOrder = 1.0
                    node.getParentPane().children.add(linkageCurrentCurve)

                } }

                /** LMB release - Disable linkage drag */
                linkShape.onMouseReleased = EventHandler { e -> run {

                    if (e.button != MouseButton.PRIMARY) {
                        return@run
                    }

                    /** Reset when mouse releases link and link is not hovered */
                    if (!linkShape.isHover) {
                        unhighlightLink(nodeLink)
                    }

                    if (linkageCurrentLinkSource != null && linkageCurrentLinkTarget != null) {
                        createLink(linkageCurrentLinkSource!!, linkageCurrentLinkTarget!!)
                    }

                    // Enable node dragging back
                    node.enableDrag()

                    // Remove a preview curve
                    node.getParentPane().children.remove(linkageCurrentCurve)
                    linkageCurrentCurve = null
                    linkageCurrentLinkSource = null
                    linkageCurrentLinkTarget = null

                } }

                /** Ctrl+LMB - Remove links */
                linkShape.onMouseClicked = EventHandler { e -> run {

                    if (e.button != MouseButton.PRIMARY) {
                        return@run
                    }

                    if (!e.isControlDown) {
                        return@run
                    }

                    breakLink(nodeLink)

                } }

            }
        }

        public fun unregisterNode(node: NodeController) {
            /** Destroy links of deleted node */
            for (nodeLink in node.getLinks()) {
                breakLink(nodeLink)
            }
        }

        public fun getLinkPos(link: LinkDef): Point2D {
            val x = link.shape.layoutX + link.shape.boundsInLocal.width / 2.0 + link.node.rootPane.translateX
            val y = link.shape.layoutY + link.shape.boundsInLocal.height / 2.0 +  link.node.rootPane.translateY
            return Point2D(x, y)
        }

        public fun findLinkage(link: LinkDef): LinkDef? {
            return linksMap[link]?.second
        }

        fun createLink(link1: LinkDef, link2: LinkDef) {
            val curve =
                if (link1.type == LinkType.IN) {
                    LinkageCurve(link2, link1)
                } else {
                    LinkageCurve(link1, link2)
                }

            curve.isMouseTransparent = true
            curve.fill = Color.TRANSPARENT
            curve.stroke = link1.valueType.color
            curve.strokeWidth = LINKAGE_STROKE_WIDTH
            curve.viewOrder = 1.0

            link1.node.getParentPane().children.add(curve)

            breakLink(link1)
            breakLink(link2)

            linksMap[link1] = Pair(curve, link2)
            linksMap[link2] = Pair(curve, link1)

            if (link1.type == LinkType.IN) {
                link1.node.updateState()
            } else {
                link2.node.updateState()
            }
        }

        fun breakLink(link: LinkDef) {
            if (linksMap.containsKey(link)) {
                val linkage = linksMap[link]
                val curve = linkage!!.first
                val link2 = linkage!!.second
                link.node.getParentPane().children.remove(curve)
                linksMap.remove(link)
                linksMap.remove(link2)

                if (link.type == LinkType.IN) {
                    link.node.updateState()
                } else {
                    link2.node.updateState()
                }
            }
        }

        private fun highlightLink(link: LinkDef) {
            link.shape.scaleX = LINK_HOVER_SCALE
            link.shape.scaleY = LINK_HOVER_SCALE
            link.shape.scaleZ = LINK_HOVER_SCALE
        }

        private fun unhighlightLink(link: LinkDef) {
            link.shape.scaleX = 1.0
            link.shape.scaleY = 1.0
            link.shape.scaleZ = 1.0
        }

        private fun areLinkable(link1: LinkDef, link2: LinkDef): Boolean {
            val bIsSameNode = link1.node == link2.node
            val bIsSameType = link1.type == link2.type
            val bIsSameValue = link1.valueType == link2.valueType
            return !bIsSameNode && !bIsSameType && bIsSameValue
        }

    }

}