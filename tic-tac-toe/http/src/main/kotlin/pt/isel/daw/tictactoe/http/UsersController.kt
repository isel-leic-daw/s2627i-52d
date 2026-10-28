package pt.isel.daw.tictactoe.http

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.tictactoe.domain.users.AuthenticatedUser
import pt.isel.daw.tictactoe.http.model.Problem
import pt.isel.daw.tictactoe.http.model.UserCreateInputModel
import pt.isel.daw.tictactoe.http.model.UserCreateTokenInputModel
import pt.isel.daw.tictactoe.http.model.UserHomeOutputModel
import pt.isel.daw.tictactoe.http.model.UserOutputModel
import pt.isel.daw.tictactoe.http.model.UserTokenCreateOutputModel
import pt.isel.daw.tictactoe.http.pipeline.RequestTokenProcessor
import pt.isel.daw.tictactoe.services.TokenCreationError
import pt.isel.daw.tictactoe.services.UserCreationError
import pt.isel.daw.tictactoe.services.UsersService
import pt.isel.daw.tictactoe.utils.Failure
import pt.isel.daw.tictactoe.utils.Success

@RestController
class UsersController(
    private val userService: UsersService,
    private val requestTokenProcessor: RequestTokenProcessor,
) {
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody input: UserCreateInputModel,
    ): ResponseEntity<*> {
        val res = userService.createUser(input.username, input.password)
        return when (res) {
            is Success -> {
                ResponseEntity
                    .status(201)
                    .header(
                        "Location",
                        Uris.Users.byId(res.value).toASCIIString(),
                    ).build<Unit>()
            }

            is Failure -> {
                when (res.value) {
                    UserCreationError.InvalidUsername -> Problem.response(400, Problem.invalidUsername)
                    UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                    UserCreationError.UserAlreadyExists -> Problem.response(409, Problem.userAlreadyExists)
                }
            }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success -> {
                val responseCookie = requestTokenProcessor.createCookie(res.value)
                ResponseEntity
                    .status(200)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))
            }

            is Failure -> {
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid -> {
                        Problem.response(400, Problem.userOrPasswordAreInvalid)
                    }
                }
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user: AuthenticatedUser): ResponseEntity<*> {
        userService.revokeToken(user.token)
        val responseCookie = requestTokenProcessor.createDeletionCookie()
        return ResponseEntity
            .status(204)
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .build<Unit>()
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val user = userService.getUserById(id) ?: return Problem.response(404, Problem.userNotFound)
        return ResponseEntity.ok(UserOutputModel(user.id, user.username))
    }

    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel =
        UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username,
        )
}
