package com.alexh.utils

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import kotlin.reflect.KClass

class Cookies private constructor() {
    init {
        noInstances(Cookies::class)
    }

    companion object {
        val USER_ID: String
        val PUZZLE_ID: String
        val JSON: String

        init {
            val doc = parseXmlConstants("cookies")
            val root = doc.documentElement

            val children = root.childNodes

            this.USER_ID = (children.item(1) as Element).tagName
            this.PUZZLE_ID = (children.item(3) as Element).tagName
            this.JSON = (children.item(5) as Element).tagName
        }
    }
}

class Endpoints private constructor() {
    init {
        noInstances(Endpoints::class)
    }

    companion object {
        val GENERATE: String
        val CREATE_USER: String
        val UPDATE_USER: String
        val READ_USER: String
        val DELETE_USER: String
        val CREATE_PUZZLE: String
        val UPDATE_PUZZLE: String
        val DELETE_PUZZLE: String
        val SHUTDOWN: String

        init {
            val doc = parseXmlConstants("endpoints")
            val root = doc.documentElement

            val children = root.childNodes

            this.GENERATE = (children.item(1) as Element).getAttribute("value")
            this.CREATE_USER = (children.item(3) as Element).getAttribute("value")
            this.READ_USER = (children.item(5) as Element).getAttribute("value")
            this.UPDATE_USER = (children.item(7) as Element).getAttribute("value")
            this.DELETE_USER = (children.item(9) as Element).getAttribute("value")
            this.CREATE_PUZZLE = (children.item(11) as Element).getAttribute("value")
            this.UPDATE_PUZZLE = (children.item(13) as Element).getAttribute("value")
            this.DELETE_PUZZLE = (children.item(15) as Element).getAttribute("value")
            this.SHUTDOWN = (children.item(17) as Element).getAttribute("value")
        }
    }
}

private fun parseXmlConstants(fileName: String): Document {
    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val file = File("C:/Users/alexh/IdeaProjects/Sudoku-Backend/Sudoku/src/main/resources/constants/${fileName}.xml")

    return builder.parse(file)
}

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw Exception("No instances of ${cls.java.name}")
}
