package pt.isel.daw.tictactoe.domain.users

import java.security.MessageDigest
import java.util.Base64

class Sha256TokenEncoder : TokenEncoder {
    override fun createValidationInformation(token: String): TokenValidationInfo = TokenValidationInfo(hash(token))

    private fun hash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        return Base64.getUrlEncoder().encodeToString(
            messageDigest.digest(
                input.toByteArray(Charsets.UTF_8),
            ),
        )
    }
}
