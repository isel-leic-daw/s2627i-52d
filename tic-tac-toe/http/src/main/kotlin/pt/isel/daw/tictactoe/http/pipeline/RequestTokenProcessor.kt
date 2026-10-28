package pt.isel.daw.tictactoe.http.pipeline

import jakarta.servlet.http.Cookie
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import pt.isel.daw.tictactoe.domain.users.AuthenticatedUser
import pt.isel.daw.tictactoe.services.TokenExternalInfo
import pt.isel.daw.tictactoe.services.UsersService
import kotlin.time.Clock

@Component
class RequestTokenProcessor(
    private val usersService: UsersService,
    private val clock: Clock,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }
        return usersService.getUserByToken(parts[1])?.let {
            AuthenticatedUser(
                it,
                parts[1],
            )
        }
    }

    fun processCookies(cookies: Array<Cookie>?): AuthenticatedUser? {
        if (cookies == null) {
            return null
        }
        val tokenCookies = cookies.filter { it.name == TOKEN_COOKIE_NAME }
        if (tokenCookies.size != 1) {
            return null
        }
        val tokenValue = tokenCookies.single().value
        return usersService.getUserByToken(tokenValue)?.let {
            AuthenticatedUser(
                it,
                tokenValue,
            )
        }
    }

    fun createCookie(tokenExternalInfo: TokenExternalInfo): ResponseCookie =
        ResponseCookie
            .from(TOKEN_COOKIE_NAME, tokenExternalInfo.tokenValue)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(tokenExternalInfo.tokenExpiration.minus(clock.now()).inWholeSeconds)
            .build()

    fun createDeletionCookie(): ResponseCookie =
        ResponseCookie
            .from(TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build()

    companion object {
        const val SCHEME = "bearer"
        const val TOKEN_COOKIE_NAME = "token"
    }
}
