package com.example.task1

import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

class CropComponent {

    enum class MouseState {
        DEFAULT,
        SELECT,
        E_RESIZE,
        W_RESIZE,
        N_RESIZE,
        S_RESIZE,
        NW_RESIZE,
        SW_RESIZE,
        NE_RESIZE,
        SE_RESIZE,
    }

    /** Crop offsets struct */
    class CropParams {
        constructor() { }

        constructor(other: CropParams) {
            left = other.left
            right = other.right
            top = other.top
            bottom = other.bottom
        }

        var left: Double = 0.0
        var right: Double = 0.0
        var top: Double = 0.0
        var bottom: Double = 0.0
    }

    private var parentPane: Pane
    private var cropViewShape: Shape? = null
    private var state: MouseState = MouseState.DEFAULT
    private var cropParams: CropParams = CropParams()

    /** Saved cursor click coordinates to calculate resize delta */
    private var clickX: Double = 0.0
    private var clickY: Double = 0.0

    public var color: Color = Color(0.0, 0.0, 0.0, 0.75)
    public var isActive: Boolean = false

    constructor(parent: Pane) {
        parentPane = parent

        parentPane.onMousePressed = EventHandler { event -> onMousePressed(event) }
        parentPane.onMouseDragged = EventHandler { event -> onMouseDragged(event) }
        parentPane.onMouseMoved = EventHandler { event -> onMouseMoved(event) }
        parentPane.onMouseReleased = EventHandler { event -> onMouseReleased(event) }
    }

    private fun onMousePressed(event: MouseEvent) {
        state = getMouseState(event)

        if (state != MouseState.DEFAULT) {
            /** Save cursor position coordinates */
            clickX = event.x
            clickY = event.y
        }
    }

    private fun onMouseReleased(event: MouseEvent) {
        /** Reset cursor and state on mouse released */
        parentPane.cursor = Cursor.DEFAULT
        state = MouseState.DEFAULT
    }

    private fun onMouseMoved(event: MouseEvent) {
        /** Select current cursor view */
        updateCursor(getMouseState(event))
    }

    private fun onMouseDragged(event: MouseEvent) {
        if (state == MouseState.DEFAULT) {
            // Ignore if no valid state or Ctrl is not down
            return
        }

        if (state == MouseState.SELECT) {
            /** Select new crop bounds */
            val localX = getLocalX(event.x)
            val localY = getLocalY(event.y)

            cropParams.left = localX
            cropParams.top = localY
            cropParams.right = parentPane.width - localX
            cropParams.bottom = parentPane.height - localY

            state = MouseState.SE_RESIZE

            updateCursor(state)
        }

        val deltaX: Double = event.x - clickX
        val deltaY: Double = event.y - clickY

        clickX = event.x
        clickY = event.y

        val newCrop = CropParams(cropParams)

        /** Right Resize */
        if (state == MouseState.E_RESIZE || state == MouseState.NE_RESIZE || state == MouseState.SE_RESIZE) {
            newCrop.right -= deltaX
        }

        /** Left Resize */
        if (state == MouseState.W_RESIZE || state == MouseState.NW_RESIZE || state == MouseState.SW_RESIZE) {
            newCrop.left += deltaX
        }

        /** Bottom Resize */
        if (state == MouseState.S_RESIZE || state == MouseState.SE_RESIZE || state == MouseState.SW_RESIZE) {
            newCrop.bottom -= deltaY
        }

        /** Top Resize */
        if (state == MouseState.N_RESIZE || state == MouseState.NW_RESIZE || state == MouseState.NE_RESIZE) {
            newCrop.top += deltaY
        }

        val maxWidth = parentPane.width
        val maxHeight = parentPane.height

        /** Clamp the crop to parent pane bounds */
        if (newCrop.left < 0.0) newCrop.left = 0.0
        else if (newCrop.left > maxWidth) newCrop.left = maxWidth
        if (newCrop.right < 0.0) newCrop.right = 0.0
        else if (newCrop.right > maxWidth) newCrop.right = maxWidth
        if (newCrop.top < 0.0) newCrop.top = 0.0
        else if (newCrop.top > maxHeight) newCrop.top = maxHeight
        if (newCrop.bottom < 0.0) newCrop.bottom = 0.0
        else if (newCrop.bottom > maxHeight) newCrop.bottom = maxHeight

        cropParams = newCrop

        redraw()
    }

    private fun getMouseState(event: MouseEvent): MouseState {
        var mouseState = MouseState.DEFAULT

        if (!isActive) {
            return mouseState
        }

        // Allow cropping only when Shift is down
        if (!event.isControlDown && event.isShiftDown) {
            /** Check whether the cursor in a resize zone */
            val left = isIntersect(cropParams.left, event.x)
            val top = isIntersect(cropParams.top, event.y)
            val right = isIntersect(parentPane.width - cropParams.right, event.x)
            val bottom = isIntersect(parentPane.height - cropParams.bottom, event.y)

            /** Select current mouse resize state */
            if (left && top)            mouseState = MouseState.NW_RESIZE
            else if (left && bottom)    mouseState = MouseState.SW_RESIZE
            else if (right && top)      mouseState = MouseState.NE_RESIZE
            else if (right && bottom)   mouseState = MouseState.SE_RESIZE
            else if (right)             mouseState = MouseState.E_RESIZE
            else if (left)              mouseState = MouseState.W_RESIZE
            else if (top)               mouseState = MouseState.N_RESIZE
            else if (bottom)            mouseState = MouseState.S_RESIZE
        }

        // Select new area only when Ctrl is down
        if (!event.isShiftDown && event.isControlDown) {
            val localX = getLocalX(event.x)
            val localY = getLocalY(event.y)

            if (localX >= 0.0 && localY >= 0.0 && localX <= parentPane.width && localY <= parentPane.height) {
                mouseState = MouseState.SELECT
            }
        }

        return mouseState
    }

    private fun updateCursor(mouseState: MouseState) {
        parentPane.cursor = when (mouseState) {
            MouseState.NW_RESIZE -> Cursor.NW_RESIZE
            MouseState.SW_RESIZE -> Cursor.SW_RESIZE
            MouseState.NE_RESIZE -> Cursor.NE_RESIZE
            MouseState.SE_RESIZE -> Cursor.SE_RESIZE
            MouseState.E_RESIZE -> Cursor.E_RESIZE
            MouseState.W_RESIZE -> Cursor.W_RESIZE
            MouseState.N_RESIZE -> Cursor.N_RESIZE
            MouseState.S_RESIZE -> Cursor.S_RESIZE
            else -> Cursor.DEFAULT
        }
    }

    private fun getLocalX(globalX: Double): Double {
        return globalX - parentPane.boundsInParent.minX
    }

    private fun getLocalY(globalY: Double): Double {
        return globalY - parentPane.boundsInParent.minY
    }

    private fun isIntersect(side: Double, point: Double): Boolean {
        val margin = 8
        return side + margin > point && side - margin < point
    }

    private fun redraw() {
        val cropRect = toRect()

        val outerRect = Rectangle()
        outerRect.x = 0.0
        outerRect.y = 0.0
        outerRect.width = parentPane.width
        outerRect.height = parentPane.height

        val innerRect = Rectangle()
        innerRect.x = cropRect.minX
        innerRect.y = cropRect.minY
        innerRect.width = cropRect.width
        innerRect.height = cropRect.height

        /** Create a crop shape subtracting inner rectangle from an outer */
        val cropShape: Shape = Shape.subtract(outerRect, innerRect)
        cropShape.fill = color

        if (cropViewShape != null)
        {
            // Invalidate old shape
            parentPane.children.remove(cropViewShape)
        }

        cropViewShape = cropShape
        parentPane.children.add(cropViewShape)
    }

    public fun resetCrop() {
        cropParams.left = 0.0
        cropParams.right = 0.0
        cropParams.top = 0.0
        cropParams.bottom = 0.0

        redraw()
    }

    public fun getCrop(): CropParams {
        val crop = CropParams(cropParams)

        /** Swap borders if crop intersects */

        val maxWidth = parentPane.width
        val maxHeight = parentPane.height

        if ((crop.left + crop.right) > maxWidth)
        {
            val cropLeftTmp = crop.left
            crop.left = maxWidth - crop.right
            crop.right = maxWidth - cropLeftTmp
        }

        if ((crop.top + crop.bottom) > maxHeight)
        {
            val cropTopTmp = crop.top
            crop.top = maxHeight - crop.bottom
            crop.bottom = maxHeight - cropTopTmp
        }

        return crop
    }

    public fun toRect(): Rectangle2D {
        val crop = getCrop()
        return Rectangle2D(
            crop.left, crop.top,
            parentPane.width - (crop.left + crop.right), parentPane.height - (crop.top + crop.bottom))
    }
}