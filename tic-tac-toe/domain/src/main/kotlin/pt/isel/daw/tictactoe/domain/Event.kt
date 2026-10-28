package pt.isel.daw.tictactoe.domain

import kotlin.time.Instant

sealed interface Event {
    data class Message(
        val id: Long,
        val msg: String,
    ) : Event

    data class KeepAlive(
        val timestamp: Instant,
    ) : Event
}
