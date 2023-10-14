package com.alexh.database

import com.alexh.plugins.checkConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet

class PuzzleService(private val connection: Connection) {
    companion object {
        const val TABLE_NAME = "Puzzles"
        const val PRIMARY_KEY = "id"
        const val SUDOKU = "infoJson"
        const val RESERVING_USER = "reservingUserId"

        private const val CREATE_PUZZLE_TABLE =
            """
            CREATE TABLE ${PuzzleService.TABLE_NAME} (
                ${PuzzleService.PRIMARY_KEY} SERIAL PRIMARY KEY,
                ${PuzzleService.SUDOKU} LONGTEXT NOT NULL,
                ${PuzzleService.RESERVING_USER} INT REFERENCES ${UserService.TABLE_NAME}(${UserService.PRIMARY_KEY}) ON UPDATE CASCADE ON DELETE CASCADE
            );
            """
        private const val GET_RESERVED_PUZZLES =
            "SELECT * FROM ${PuzzleService.TABLE_NAME} WHERE ${PuzzleService.RESERVING_USER} = ?;"
        private const val DELETE_PUZZLE =
            "DELETE FROM ${PuzzleService.TABLE_NAME} WHERE ${PuzzleService.PRIMARY_KEY} = ?;"
    }

    suspend fun getReservedPuzzles(userId: Int): List<Puzzle> = withContext(Dispatchers.IO) {
        checkConnection(this@PuzzleService.connection)

        val statement = this@PuzzleService.connection.prepareStatement(PuzzleService.GET_RESERVED_PUZZLES)

        statement.setInt(1, userId)

        val result = statement.executeQuery()

        return@withContext this@PuzzleService.convertToList(result)
    }

    private fun convertToList(result: ResultSet): List<Puzzle> {
        val list = mutableListOf<Puzzle>()

        while (result.next()) {
            val id = result.getInt(PuzzleService.PRIMARY_KEY)
            val info = result.getString(PuzzleService.SUDOKU)
            val reservingUser = result.getInt(PuzzleService.RESERVING_USER)

            val puzzle = Puzzle(id, info, reservingUser)

            list.add(puzzle)
        }

        return list
    }

    suspend fun deletePuzzle(id: Int): Unit = withContext(Dispatchers.IO) {
        checkConnection(this@PuzzleService.connection)

        val statement = this@PuzzleService.connection.prepareStatement(PuzzleService.DELETE_PUZZLE)

        statement.setInt(1, id)

        statement.executeUpdate()
    }
}