package pt.isel.daw.tictactoe.domain.users

import jakarta.inject.Named
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.SecureRandom
import java.util.Base64
import kotlin.time.Clock
import kotlin.time.Instant

@Named
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
) {
    // SecureRandom is thread-safe, so a single instance can be shared
    private val secureRandom = SecureRandom()

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            secureRandom.nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean =
        try {
            Base64
                .getUrlDecoder()
                .decode(token)
                .size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = checkNotNull(passwordEncoder.encode(password)),
        )

    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
            (now - token.createdAt) <= config.tokenTtl &&
            (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String): TokenValidationInfo = tokenEncoder.createValidationInformation(token)

    // TODO it could be better
    fun isSafePassword(password: String) = password.length > 4

    fun isValidUsername(username: String) = username.isNotBlank() && username.length <= MAX_USERNAME_LENGTH

    val maxNumberOfTokensPerUser = config.maxTokensPerUser

    companion object {
        // Must match the size of the username column on the database schema
        const val MAX_USERNAME_LENGTH = 64
    }
}
