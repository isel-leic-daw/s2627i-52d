package pt.isel.daw.tictactoe.http.pipeline

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogFilter : HttpFilter() {
    override fun doFilter(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain,
    ) {
        try {
            chain.doFilter(req, res)
            logger.info("status: {}", res.status)
        } catch (ex: Exception) {
            logger.warn("Exception", ex)
            throw ex
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LogFilter::class.java)
    }
}
