package com.example.task2

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/** A node definition for enumeration in the menu */
class NodeDef {

    enum class NodeType {
        INPUT,
        FUNCTION,
    }

    // A human-readable display name
    public var name: String

    // A path to the resource FXML file
    public var resource: String

    // A type category of the node
    public var type: NodeType

    constructor(name: String, resource: String, type: NodeType) {
        this.name = name
        this.resource = resource
        this.type = type
    }
}

class AppController {

    private val IMAGE_INPUT_POS_DEF = Point2D(100.0, 200.0)

    private val IMAGE_OUTPUT_POS_DEF = Point2D(800.0, 200.0)

    @FXML
    private lateinit var rootCanvas: Pane

    @FXML
    private lateinit var menuItemNew: MenuItem

    @FXML
    private lateinit var menuItemLoad: MenuItem

    @FXML
    private lateinit var menuItemSave: MenuItem

    @FXML
    private lateinit var menuItemImport: MenuItem

    @FXML
    private lateinit var menuItemExport: MenuItem

    @FXML
    private lateinit var menuInputNodes: Menu

    @FXML
    private lateinit var menuFunctionNodes: Menu

    private val nodesArray: Array<NodeDef> = arrayOf(
        NodeDef( "Float Input", "float-input.fxml", NodeDef.NodeType.INPUT ),
        NodeDef( "Integer Input", "int-input.fxml", NodeDef.NodeType.INPUT ),
        NodeDef( "String Input", "string-input.fxml", NodeDef.NodeType.INPUT ),
        NodeDef( "Image Input", "image-input.fxml", NodeDef.NodeType.INPUT ),

        NodeDef( "Add Text", "add-text-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Add Image", "add-image-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Gray Filter", "gray-filter-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Brightness", "brightness-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Sepia Filter", "sepia-filter-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Invert Filter", "invert-filter-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Gaussian Blur", "gaussian-blur-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Transform Move", "transform-move-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Transform Scale", "transform-scale-node.fxml", NodeDef.NodeType.FUNCTION ),
        NodeDef( "Transform Rotate", "transform-rotate-node.fxml", NodeDef.NodeType.FUNCTION ),
    )

    private var spawnedNodes: MutableList<NodeController> = mutableListOf()

    @FXML
    private fun onMenuItemNew() {
        initializeScene()
    }

    @FXML
    private fun onMenuItemLoad() {
        loadScene()
    }

    @FXML
    private fun onMenuItemSave() {
        saveScene()
    }

    @FXML
    private fun onMenuItemImport() {
        /** Import source image */
        val imageFile = ImageUtils.loadImage(rootCanvas.scene.window)
        if (imageFile != null) {
            getInputNode().imageViewSource?.image = imageFile
            getInputNode().updateState()
        }
    }

    @FXML
    private fun onMenuItemExport() {
        /** Export source image */
        val outImage: Image? = getOutputNode().getImage()
        if (outImage != null) {
            ImageUtils.saveImage(rootCanvas.scene.window, outImage)
        }
    }

    fun init() {
        // Make root canvas dynamic
        DynamicSceneSystem.makeDynamicPane(rootCanvas)

        /** Add available nodes to the list in the menu */
        for (nodeDef in nodesArray) {

            /** Switch between node menus depending on the type*/
            val nodeMenu: Menu = when (nodeDef.type) {
                NodeDef.NodeType.INPUT -> menuInputNodes
                NodeDef.NodeType.FUNCTION -> menuFunctionNodes
            }

            val nodeMenuItem = MenuItem(nodeDef.name)

            /** Bind node spawning functionality to the action event */
            nodeMenuItem.onAction = EventHandler { run {
                spawnNode(nodeDef.resource, -rootCanvas.translateX, -rootCanvas.translateY)
            } }

            nodeMenu.items.add(nodeMenuItem)
        }

        initializeScene()
    }

    private fun initializeScene() {
        clearScene()

        /** Create default nodes */
        spawnNode("global-input.fxml", IMAGE_INPUT_POS_DEF.x, IMAGE_INPUT_POS_DEF.y)
        spawnNode("global-output.fxml", IMAGE_OUTPUT_POS_DEF.x, IMAGE_OUTPUT_POS_DEF.y)
    }

    private fun clearScene() {
        /** Remove old nodes */
        val spawnedNodesCopy = mutableListOf<NodeController>()
        spawnedNodesCopy.addAll(spawnedNodes)
        for (node in spawnedNodesCopy) {
            node.destroyNode()
        }

        spawnedNodes.clear()

        /** Reset canvas position */
        rootCanvas.translateX = 0.0
        rootCanvas.translateY = 0.0
    }

    private fun getInputNode(): ImageSourceNodeController {
        // First node might always be input source
        return spawnedNodes[0] as ImageSourceNodeController
    }

    private fun getOutputNode(): OutputNodeController {
        // Second node might always be result output
        return spawnedNodes[1] as OutputNodeController
    }

    private fun saveScene() {
        val choosenFile = FileUtils
            .getFileChooser("Save Scene", "Scene Files", "*.scene")
            .showSaveDialog(rootCanvas.scene.window)

        if (choosenFile == null) {
            return
        }

        val outStream = ObjectOutputStream(FileOutputStream(choosenFile))

        outStream.writeDouble(rootCanvas.translateX)
        outStream.writeDouble(rootCanvas.translateY)

        outStream.writeInt(spawnedNodes.size)

        for (node in spawnedNodes) {
            outStream.writeUTF(node.resource)
            node.serialize(outStream)
        }

        /** Save links in format { node1Id, node1OutLinkId, node2Id, node2InLinkId } */
        val linksData: MutableList<Array<Int>> = mutableListOf()

        var nodeId = 0
        for (node in spawnedNodes) {
            var linkId = 0
            for (link in node.getLinks()) {
                if (link.type == LinkType.OUT) {
                    val otherLink = LinkSystem.findLinkage(link)
                    var otherNodeId = spawnedNodes.indexOf(otherLink!!.node)
                    var otherLinkId = otherLink!!.node.getLinks().indexOf(otherLink)
                    linksData.add(arrayOf(nodeId, linkId, otherNodeId, otherLinkId))
                }
                ++linkId
            }
            ++nodeId
        }

        outStream.writeInt(linksData.size)

        for (link in linksData) {
            outStream.writeInt(link[0])
            outStream.writeInt(link[1])
            outStream.writeInt(link[2])
            outStream.writeInt(link[3])
        }

        outStream.close()
    }

    private fun loadScene() {
        val choosenFile = FileUtils
            .getFileChooser("Load Scene", "Scene Files", "*.scene")
            .showOpenDialog(rootCanvas.scene.window)

        if (choosenFile == null) {
            return
        }

        clearScene()

        val inStream = ObjectInputStream(FileInputStream(choosenFile))

        rootCanvas.translateX = inStream.readDouble()
        rootCanvas.translateY = inStream.readDouble()

        val nodesNum = inStream.readInt()

        for (i in 0 until nodesNum) {
            spawnNode(inStream.readUTF(), 0.0, 0.0).deserialize(inStream)
        }

        val linksNum = inStream.readInt()

        for (i in 0 until linksNum) {
            val nodeId = inStream.readInt()
            val linkId = inStream.readInt()
            val otherNodeId = inStream.readInt()
            val otherLinkId = inStream.readInt()

            LinkSystem.createLink(
                spawnedNodes[nodeId].getLinks()[linkId],
                spawnedNodes[otherNodeId].getLinks()[otherLinkId])
        }

        inStream.close()
    }

    private fun spawnNode(resource: String, posX: Double, posY: Double): NodeController {
        val fxmlLoader = FXMLLoader(EditorApplication::class.java.getResource(resource))

        val node = fxmlLoader.load<Node>()
        node.translateX = posX
        node.translateY = posY
        rootCanvas.children.add(node)

        val controller = fxmlLoader.getController<NodeController>()
        controller.resource = resource
        controller.initNode()

        spawnedNodes.add(controller)

        controller.onDestroyCb = {
            spawnedNodes.remove(controller)
        }

        return controller
    }
}