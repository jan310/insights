package jan.ondra.insights.api

import jan.ondra.insights.business.UserService
import jan.ondra.insights.models.User
import jan.ondra.insights.util.getUserIdFromBearerToken
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("http://localhost:3000")
class UserController(private val userService: UserService) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun registerUser(@RequestHeader(AUTHORIZATION) bearerToken: String, @RequestBody emailDto: EmailDto) {
        emailDto.validate()
        userService.create(User(
            id = getUserIdFromBearerToken(bearerToken),
            email = emailDto.email
        ))
    }

    @PatchMapping
    @ResponseStatus(NO_CONTENT)
    fun updateUserEmail(@RequestHeader(AUTHORIZATION) bearerToken: String, @RequestBody emailDto: EmailDto) {
        emailDto.validate()
        userService.update(User(
            id = getUserIdFromBearerToken(bearerToken),
            email = emailDto.email
        ))
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    fun deleteUser(@RequestHeader(AUTHORIZATION) bearerToken: String) {
        userService.delete(id = getUserIdFromBearerToken(bearerToken))
    }

}
