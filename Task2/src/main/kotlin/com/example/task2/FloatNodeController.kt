package com.example.task2

import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.shape.Shape
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FloatNodeController : NodeController() {
    @FXML
    private lateinit var outLinkFloat: Shape

    @FXML
    private lateinit var textFieldInput: TextField

    private lateinit var linkDefOutFloat: LinkDef

    override fun initNode() {
        linkDefOutFloat = LinkDef(this, outLinkFloat, "Output Float", LinkType.OUT, LinkValueType.FLOAT)

        textFieldInput.textProperty().addListener { _: ObservableValue<out String>, oldValue: String, newValue: String ->
            if (newValue.toFloatOrNull() == null) {
                textFieldInput.text = oldValue
            } else {
                updateState()
            }
        }

        super.initNode()
    }

    fun getFloat() : Float {
        return textFieldInput.text.toFloat()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefOutFloat,
        )
    }

    override fun serialize(stream: ObjectOutputStream) {
        super.serialize(stream)

        stream.writeUTF(textFieldInput.text)
    }

    override fun deserialize(stream: ObjectInputStream) {
        super.deserialize(stream)

        textFieldInput.text = stream.readUTF()
    }
}