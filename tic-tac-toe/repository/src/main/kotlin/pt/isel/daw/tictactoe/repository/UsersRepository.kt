package pt.isel.daw.tictactoe.repository

import pt.isel.daw.tictactoe.domain.users.PasswordValidationInfo
import pt.isel.daw.tictactoe.domain.users.Token
import pt.isel.daw.tictactoe.domain.users.TokenValidationInfo
import pt.isel.daw.tictactoe.domain.users.User
import kotlin.time.Instant

interface UsersRepository {
    /**
     * Stores a new user, returning its id, or null if a user with the same username already exists.
     */
    fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo,
    ): Int?

    fun getUserById(id: Int): User?

    fun getUserByUsername(username: String): User?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
