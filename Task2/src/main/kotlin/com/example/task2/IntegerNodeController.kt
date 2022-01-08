package com.example.task2

import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.shape.Shape
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class IntegerNodeController : NodeController() {
    @FXML
    private lateinit var outLinkInt: Shape

    @FXML
    private lateinit var textFieldInput: TextField

    private lateinit var linkDefOutInt: LinkDef

    override fun initNode() {
        linkDefOutInt = LinkDef(this, outLinkInt, "Output Integer", LinkType.OUT, LinkValueType.INTEGER)

        textFieldInput.textProperty().addListener { _: ObservableValue<out String>, oldValue: String, newValue: String ->
            if (newValue.toIntOrNull() == null) {
                textFieldInput.text = oldValue
            } else {
                updateState()
            }
        }

        super.initNode()
    }

    fun getInteger() : Int {
        return textFieldInput.text.toInt()
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefOutInt,
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