package pt.isel.daw.tictactoe

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class TestClock : Clock {
    // Initialized this way to reduce precision to seconds
    private var testNow: Instant = Instant.fromEpochSeconds(Clock.System.now().epochSeconds)

    fun advance(duration: Duration) {
        testNow = testNow.plus(duration)
    }

    override fun now() = testNow
}
