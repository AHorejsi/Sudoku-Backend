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
        val DIMENSION: String
        val DIFFICULTY: String
        val GAMES: String
        val USER_ID: String
        val USERNAME: String
        val EMAIL: String
        val PASSWORD: String
        val PUZZLE_ID: String
        val JSON: String

        init {
            val doc = parseXmlConstants("cookies")
            val root = doc.documentElement

            val children = root.childNodes

            this.DIMENSION = (children.item(1) as Element).tagName
            this.DIFFICULTY = (children.item(3) as Element).tagName
            this.GAMES = (children.item(5) as Element).tagName
            this.USER_ID = (children.item(7) as Element).tagName
            this.USERNAME = (children.item(9) as Element).tagName
            this.EMAIL = (children.item(11) as Element).tagName
            this.PASSWORD = (children.item(13) as Element).tagName
            this.PUZZLE_ID = (children.item(15) as Element).tagName
            this.JSON = (children.item(17) as Element).tagName
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

class JwtClaims private constructor() {
    init {
        noInstances(JwtClaims::class)
    }

    companion object {
        val OP_KEY: String
        val ADMIN_USER_KEY: String
        val ADMIN_PASSWORD_KEY: String

        val GENERATE_PUZZLE_VALUE: String
        val CREATE_USER_VALUE: String
        val READ_USER_VALUE: String
        val UPDATE_USER_VALUE: String
        val DELETE_USER_VALUE: String
        val CREATE_PUZZLE_VALUE: String
        val UPDATE_PUZZLE_VALUE: String
        val DELETE_PUZZLE_VALUE: String

        val SHUTDOWN_VALUE: String

        init {
            val doc = parseXmlConstants("jwt_claims")
            val root = doc.documentElement

            val children = root.childNodes

            this.OP_KEY = (children.item(1) as Element).getAttribute("key")
            this.ADMIN_USER_KEY = (children.item(3) as Element).getAttribute("key")
            this.ADMIN_PASSWORD_KEY = (children.item(5) as Element).getAttribute("key")

            this.GENERATE_PUZZLE_VALUE = (children.item(7) as Element).getAttribute("value")
            this.CREATE_USER_VALUE = (children.item(9) as Element).getAttribute("value")
            this.READ_USER_VALUE = (children.item(11) as Element).getAttribute("value")
            this.UPDATE_USER_VALUE = (children.item(13) as Element).getAttribute("value")
            this.DELETE_USER_VALUE = (children.item(15) as Element).getAttribute("value")
            this.CREATE_PUZZLE_VALUE = (children.item(17) as Element).getAttribute("value")
            this.UPDATE_PUZZLE_VALUE = (children.item(19) as Element).getAttribute("value")
            this.DELETE_PUZZLE_VALUE = (children.item(21) as Element).getAttribute("value")
            this.SHUTDOWN_VALUE = (children.item(23) as Element).getAttribute("value")
        }
    }
}

private fun parseXmlConstants(fileName: String): Document {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val file = File("C:/Users/alexh/IdeaProjects/Sudoku-Backend/Sudoku/src/main/resources/constants/${fileName}.xml")

    return builder.parse(file)
}

private fun <T : Any> noInstances(cls: KClass<T>): Nothing {
    throw Exception("No instances of ${cls.java.name}")
}
