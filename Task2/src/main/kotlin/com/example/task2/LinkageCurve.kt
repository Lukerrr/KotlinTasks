package com.example.task2

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.shape.CubicCurve

class LinkageCurve: CubicCurve {

    companion object {

        // An offset for linking cubic curves control points (along X axis)
        private const val LINK_CURVE_SWING = 250.0

        public fun setupCubicCurve(curve: CubicCurve, start: Point2D, end: Point2D) {
            curve.startX = start.x
            curve.startY = start.y
            curve.endX = end.x
            curve.endY = end.y

            curve.controlY1 = curve.startY
            curve.controlY2 = curve.endY

            val distanceX: Double = kotlin.math.abs(curve.startX - curve.endX)
            val swingX = kotlin.math.min(distanceX, LINK_CURVE_SWING)

            curve.controlX1 = curve.startX + swingX
            curve.controlX2 = curve.endX - swingX
        }
    }

    private val link1: LinkDef
    private val link2: LinkDef

    constructor(link1: LinkDef, link2: LinkDef) {
        this.link1 = link1
        this.link2 = link2

        val nodeChangeListener = { _: ObservableValue<out Number>, _: Number, _: Number ->
            updateCurve()
        }

        this.link1.node.rootPane.translateXProperty().addListener(nodeChangeListener)
        this.link1.node.rootPane.translateYProperty().addListener(nodeChangeListener)
        this.link2.node.rootPane.translateXProperty().addListener(nodeChangeListener)
        this.link2.node.rootPane.translateYProperty().addListener(nodeChangeListener)

        updateCurve()
    }

    private fun updateCurve() {
        val startPt = LinkSystem.getLinkPos(link1)
        val endPt = LinkSystem.getLinkPos(link2)
        setupCubicCurve(this, startPt, endPt)
    }
}