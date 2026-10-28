package pt.isel.daw.tictactoe.services

import jakarta.inject.Named
import org.slf4j.LoggerFactory
import pt.isel.daw.tictactoe.domain.Event
import pt.isel.daw.tictactoe.domain.EventEmitter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Clock

@Named
class ChatService(
    private val clock: Clock,
) : NeedsShutdown {
    // Important: mutable state on a singleton service
    private val emitters = mutableListOf<EventEmitter>()
    private var currentId = 0L

    private var closing = false
    private val lock = ReentrantLock()

    // A scheduler to send the periodic keep-alive events
    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(1).also {
            it.scheduleAtFixedRate({ keepAlive() }, 2, 2, TimeUnit.SECONDS)
        }

    override fun shutdown() {
        logger.info("shutting down")
        lock.withLock {
            closing = true
            val id = currentId++
            sendEventToAll(Event.Message(id, "bye"))
            // Iterate over a copy, since completing an emitter may trigger its removal from the list
            val emittersToComplete = emitters.toList()
            emitters.clear()
            emittersToComplete.forEach {
                it.complete()
            }
        }

        scheduler.shutdown()
    }

    fun addEventEmitter(emitter: EventEmitter) =
        lock.withLock {
            logger.info("adding emitter")
            emitters.add(emitter)
            emitter.onCompletion {
                logger.info("onCompletion")
                removeEmitter(emitter)
            }
            emitter.onError {
                logger.info("onError")
                removeEmitter(emitter)
            }
            emitter
        }

    fun sendMessage(msg: String) =
        lock.withLock {
            logger.info("sendMessage")
            val id = currentId++
            sendEventToAll(Event.Message(id, msg))
        }

    private fun removeEmitter(emitter: EventEmitter) =
        lock.withLock {
            logger.info("removing emitter")
            emitters.remove(emitter)
        }

    private fun keepAlive() =
        lock.withLock {
            if (emitters.isEmpty()) {
                return@withLock
            }
            logger.info("keepAlive, sending to {} emitters", emitters.count())
            sendEventToAll(Event.KeepAlive(clock.now()))
        }

    // Must be called while holding the lock
    private fun sendEventToAll(event: Event) {
        val failedEmitters = mutableListOf<EventEmitter>()
        emitters.forEach {
            try {
                it.emit(event)
            } catch (ex: Exception) {
                logger.info("Exception while sending event - {}", ex.message)
                failedEmitters.add(it)
            }
        }
        // Emitters that failed are no longer usable, so stop sending events to them
        emitters.removeAll(failedEmitters)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatService::class.java)
    }
}
