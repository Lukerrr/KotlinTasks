package com.example.task2

import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.shape.Shape
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class StringNodeController : NodeController() {
    @FXML
    private lateinit var outLinkString: Shape

    @FXML
    private lateinit var textFieldInput: TextField

    private lateinit var linkDefOutString: LinkDef

    override fun initNode() {
        linkDefOutString = LinkDef(this, outLinkString, "Output String", LinkType.OUT, LinkValueType.STRING)

        textFieldInput.textProperty().addListener { _: ObservableValue<out String>, _: String, _: String ->
            updateState()
        }

        super.initNode()
    }

    fun getString() : String {
        return textFieldInput.text
    }

    override fun getLinks(): Array<LinkDef> {
        return super.getLinks() + arrayOf(
            linkDefOutString,
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