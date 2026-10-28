package pt.isel.daw.tictactoe

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component
import pt.isel.daw.tictactoe.services.NeedsShutdown

@Component
class ShutdownManager(
    private val needShutdown: List<NeedsShutdown>,
) : ApplicationListener<ContextClosedEvent> {
    override fun onApplicationEvent(event: ContextClosedEvent) {
        logger.info("on ContextClosedEvent")
        needShutdown.forEach {
            it.shutdown()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ShutdownManager::class.java)
    }
}
