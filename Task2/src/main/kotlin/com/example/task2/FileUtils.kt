package com.example.task2

import javafx.stage.FileChooser

class FileUtils {

    companion object {

        fun getFileChooser(title: String, extName: String, vararg extentions: String): FileChooser {
            val fileChooser = FileChooser()
            fileChooser.title = title
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(extName, *extentions))
            return fileChooser
        }

    }

}
