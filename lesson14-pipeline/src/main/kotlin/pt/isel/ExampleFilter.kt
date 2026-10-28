package pt.isel

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * A servlet filter contributes to the handling of HTTP responses, by using [HttpServletRequest]
 * and eventually mutating [HttpServletResponse] *before* and *after* the request is handled by a server.
 * Multiple filters are organized in a pipeline.
 */

@Component
class ExampleFilter : HttpFilter() {
    override fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        // Performed before the request is handled by the servlet
        val start = System.nanoTime()
        log.info("doFilter: before chain call")

        // Call the rest of the pipeline
        // QUESTION what happens if a filter does not call chain.doFilter?
        chain.doFilter(request, response)

        // Performed after the request was handled by the servlet
        val end = System.nanoTime()
        val delta = (end - start) / 1000
        log.info("doFilter: after chain call, took: {} us", delta)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ExampleFilter::class.java)
    }
}
