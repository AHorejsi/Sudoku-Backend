package com.alexh.database

import java.sql.Connection

class PuzzleService(private val connection: Connection) {
    private companion object {
        const val CREATE_PUZZLE_TABLE =
            """CREATE TABLE Puzzles (
                id SERIAL PRIMARY KEY,
                info LONGTEXT NOT NULL,
                reservedBy INT REFERENCES Users(id) ON UPDATE CASCADE ON DELETE CASCADE
            );"""
    }

    init {
        val statement = this.connection.createStatement()

        statement.executeUpdate(PuzzleService.CREATE_PUZZLE_TABLE)
    }
}