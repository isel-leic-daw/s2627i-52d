package pt.isel.daw.tictactoe.domain.games

import kotlin.test.Test
import kotlin.test.assertEquals

class BoardTests {
    @Test
    fun `can serialize and deserialize`() {
        val board = Board.create()
        val s = board.toString()
        assertEquals("         ", s)

        val newBoard = Board.fromString(s)
        assertEquals(board, newBoard)
    }

    @Test
    fun `serialization is done by rows`() {
        val board = Board.create()
        val newBoard = board.mutate(Position(0, 1), Board.State.PLAYER_X)
        assertEquals("   X     ", newBoard.toString())
    }

    @Test
    fun `mutate does not change the original board`() {
        val board = Board.create()
        board.mutate(Position(0, 1), Board.State.PLAYER_X)
        assertEquals(Board.create(), board)
    }
}
