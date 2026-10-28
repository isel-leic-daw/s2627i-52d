package pt.isel.daw.tictactoe

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class TestClock : Clock {
    private var now = Instant.fromEpochSeconds(0)

    override fun now(): Instant = now

    fun advance(duration: Duration) {
        now += duration
    }
}
