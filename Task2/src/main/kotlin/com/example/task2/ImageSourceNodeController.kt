package com.example.task2

import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer


open class ImageSourceNodeController : ImageNodeController() {
    override fun serialize(stream: ObjectOutputStream) {
        super.serialize(stream)

        val bHasImage = imageViewSource.image != null
        stream.writeBoolean(bHasImage)

        if (bHasImage) {
            val format: WritablePixelFormat<ByteBuffer> = WritablePixelFormat.getByteBgraInstance()

            val w = imageViewSource.image.width.toInt()
            val h = imageViewSource.image.height.toInt()

            val scanlineStride = w * 4
            val dataSize = w * h * 4

            val data = ByteArray(dataSize)

            imageViewSource.image.pixelReader.getPixels(0, 0, w, h, format, data, 0, scanlineStride)

            stream.writeInt(w)
            stream.writeInt(h)
            stream.write(data)
        }
    }

    override fun deserialize(stream: ObjectInputStream) {
        super.deserialize(stream)

        val bHasImage = stream.readBoolean()

        if (bHasImage) {
            val w = stream.readInt()
            val h = stream.readInt()
            val dataSize = w * h * 4
            val scanlineStride = w * 4
            val data = stream.readNBytes(dataSize)

            val image = WritableImage(w, h)

            val format: WritablePixelFormat<ByteBuffer> = WritablePixelFormat.getByteBgraInstance()
            image.pixelWriter.setPixels(0, 0, w, h, format, data, 0, scanlineStride)

            imageViewSource.image = image
        }
    }
}