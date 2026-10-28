package pt.isel.daw.tictactoe.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI,
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val userAlreadyExists =
            Problem(
                URI(
                    "https://github.com/isel-leic-daw/s2627i-51d-51n/tree/main/code/jvm/tic-tac-toe/" +
                        "docs/problems/user-already-exists",
                ),
            )
        val invalidUsername =
            Problem(
                URI(
                    "https://github.com/isel-leic-daw/s2627i-51d-51n/tree/main/code/jvm/tic-tac-toe/" +
                        "docs/problems/invalid-username",
                ),
            )
        val insecurePassword =
            Problem(
                URI(
                    "https://github.com/isel-leic-daw/s2627i-51d-51n/tree/main/code/jvm/tic-tac-toe/" +
                        "docs/problems/insecure-password",
                ),
            )

        val userOrPasswordAreInvalid =
            Problem(
                URI(
                    "https://github.com/isel-leic-daw/s2627i-51d-51n/tree/main/code/jvm/tic-tac-toe/" +
                        "docs/problems/user-or-password-are-invalid",
                ),
            )

        val userNotFound =
            Problem(
                URI(
                    "https://github.com/isel-leic-daw/s2627i-51d-51n/tree/main/code/jvm/tic-tac-toe/" +
                        "docs/problems/user-not-found",
                ),
            )

        val invalidRequestContent =
            Problem(
                URI(
                    "https://github.com/isel-leic-daw/s2627i-51d-51n/tree/main/code/jvm/tic-tac-toe/" +
                        "docs/problems/invalid-request-content",
                ),
            )
    }
}
