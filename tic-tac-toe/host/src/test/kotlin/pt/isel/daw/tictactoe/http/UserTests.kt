package pt.isel.daw.tictactoe.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.daw.tictactoe.http.model.TokenResponse
import java.time.Duration
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTests {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `can create an user`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val username = newTestUserName()
        val password = "changeit"

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client
            .post()
            .uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                ),
            ).exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }
    }

    @Test
    fun `can create an user, obtain a token, and access user home, and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val username = newTestUserName()
        val password = "changeit"

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client
            .post()
            .uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                ),
            ).exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }

        // when: creating a token
        // then: the response is a 200
        val result =
            client
                .post()
                .uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to username,
                        "password" to password,
                    ),
                ).exchange()
                .expectStatus()
                .isOk
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseBody!!

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client
            .get()
            .uri("/me")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("username")
            .isEqualTo(username)

        // when: getting the user home with an invalid token
        // then: the response is a 4001 with the proper problem
        client
            .get()
            .uri("/me")
            .header("Authorization", "Bearer ${result.token}-invalid")
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .valueEquals("WWW-Authenticate", "bearer")

        // when: revoking the token
        // then: response is a 200
        client
            .post()
            .uri("/logout")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus()
            .isNoContent

        // when: getting the user home with the revoked token
        // then: response is a 401
        client
            .get()
            .uri("/me")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .valueEquals("WWW-Authenticate", "bearer")
    }

    @Test
    fun `can create an user, obtain a cookie, and access user home, and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val username = newTestUserName()
        val password = "changeit"

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client
            .post()
            .uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                ),
            ).exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }

        // when: creating a token
        // then: the response is a 200 with a token cookie
        val cookies =
            client
                .post()
                .uri("/users/token")
                .bodyValue(
                    mapOf(
                        "username" to username,
                        "password" to password,
                    ),
                ).exchange()
                .expectStatus()
                .isOk
                .expectLoginCookie()
                .expectBody(TokenResponse::class.java)
                .returnResult()
                .responseCookies
        val tokenCookie = cookies["token"]?.single()
        assertNotNull(tokenCookie)

        // when: getting the user home WITHOUT an authorization header and WITH a cookie
        // then: the response is a 200 with the proper representation
        client
            .get()
            .uri("/me")
            .cookies { it.add("token", tokenCookie.value) }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("username")
            .isEqualTo(username)

        // when: revoking the token
        // then: response is a 200
        client
            .post()
            .uri("/logout")
            .cookies { it.add("token", tokenCookie.value) }
            .exchange()
            .expectRemoveLoginCookie()
            .expectStatus()
            .isNoContent

        // when: getting the user home again
        // then: response is a 401
        client
            .get()
            .uri("/me")
            .cookies { it.add("token", tokenCookie.value) }
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .valueEquals("WWW-Authenticate", "bearer")
    }

    companion object {
        private fun newTestUserName() = "user-${abs(Random.nextLong())}"
    }
}

private fun WebTestClient.ResponseSpec.expectLoginCookie(): WebTestClient.ResponseSpec {
    this.expectCookie().apply {
        this.exists("token")
        this.sameSite("token", "Strict")
        this.secure("token", true)
        this.httpOnly("token", true)
    }

    return this
}

private fun WebTestClient.ResponseSpec.expectRemoveLoginCookie(): WebTestClient.ResponseSpec {
    this.expectCookie().apply {
        this.exists("token")
        this.sameSite("token", "Strict")
        this.secure("token", true)
        this.httpOnly("token", true)
        this.maxAge("token", Duration.ZERO)
    }

    return this
}
