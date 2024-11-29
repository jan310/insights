package jan.ondra.insights.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import jan.ondra.insights.business.UserService
import jan.ondra.insights.exception.EmailAlreadyExistsException
import jan.ondra.insights.exception.UserAlreadyRegisteredException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.FilterTag.PERSONAL_DEVELOPMENT
import jan.ondra.insights.models.FilterTag.WEALTH_CREATION
import jan.ondra.insights.models.User
import jan.ondra.insights.util.USER_1_BEARER_TOKEN
import jan.ondra.insights.util.USER_1_EMAIL
import jan.ondra.insights.util.USER_1_ID
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerMvcTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var userService: UserService

    @Nested
    inner class CreateUser {

        @Test
        fun `should succeed`() {
            justRun { userService.createUser(any()) }

            mockMvc
                .perform(post("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(
                        ObjectMapper().writeValueAsString(
                            UserDto(
                                email = USER_1_EMAIL,
                                notificationEnabled = false,
                                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                            )
                        )
                    )
                )
                .andExpectAll(
                    status().isCreated,
                    content().string("")
                )
        }

        @Test
        fun `should fail when user ID already exists`() {
            every { userService.createUser(any()) } throws UserAlreadyRegisteredException(DuplicateKeyException(null))

            mockMvc
                .perform(post("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(
                        ObjectMapper().writeValueAsString(
                            UserDto(
                                email = USER_1_EMAIL,
                                notificationEnabled = false,
                                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                            )
                        )
                    )
                )
                .andExpectAll(
                    status().isConflict,
                    content().string("Registration failed")
                )
        }

        @Test
        fun `should fail when email already exists`() {
            every { userService.createUser(any()) } throws EmailAlreadyExistsException(DuplicateKeyException(null))

            mockMvc
                .perform(post("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(
                        ObjectMapper().writeValueAsString(
                            UserDto(
                                email = USER_1_EMAIL,
                                notificationEnabled = false,
                                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                            )
                        )
                    )
                )
                .andExpectAll(
                    status().isConflict,
                    content().string("The email already exists")
                )
        }

    }

    @Nested
    inner class GetUser {

        @Test
        fun `should succeed`() {
            val user = User(
                id = USER_1_ID,
                email = USER_1_EMAIL,
                notificationEnabled = false,
                notificationFilterTags = listOf()
            )

            every { userService.getUser(any()) } returns user

            mockMvc
                .perform(get("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isOk,
                    content().json(ObjectMapper().writeValueAsString(user))
                )
        }

        @Test
        fun `should fail when user does not exist`() {
            every { userService.getUser(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(get("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isConflict,
                    content().string("User is not registered")
                )
        }

    }

    @Nested
    inner class UpdateUser {

        @Test
        fun `should succeed`() {
            justRun { userService.updateUser(any()) }

            mockMvc
                .perform(put("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(
                        ObjectMapper().writeValueAsString(
                            UserDto(
                                email = USER_1_EMAIL,
                                notificationEnabled = false,
                                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                            )
                        )
                    )
                )
                .andExpectAll(
                    status().isNoContent,
                    content().string("")
                )
        }

        @Test
        fun `should fail when user does not exist`() {
            every { userService.updateUser(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(put("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(
                        ObjectMapper().writeValueAsString(
                            UserDto(
                                email = USER_1_EMAIL,
                                notificationEnabled = false,
                                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                            )
                        )
                    )
                )
                .andExpectAll(
                    status().isConflict,
                    content().string("User is not registered")
                )
        }

        @Test
        fun `should fail when email already exists`() {
            every { userService.updateUser(any()) } throws EmailAlreadyExistsException(DuplicateKeyException(null))

            mockMvc
                .perform(put("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(
                        ObjectMapper().writeValueAsString(
                            UserDto(
                                email = USER_1_EMAIL,
                                notificationEnabled = false,
                                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                            )
                        )
                    )
                )
                .andExpectAll(
                    status().isConflict,
                    content().string("The email already exists")
                )
        }

    }

    @Nested
    inner class DeleteUser {

        @Test
        fun `should succeed`() {
            justRun { userService.deleteUser(any()) }

            mockMvc
                .perform(delete("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNoContent,
                    content().string("")
                )
        }

        @Test
        fun `should fail when user does not exist`() {
            every { userService.deleteUser(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(delete("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isConflict,
                    content().string("User is not registered")
                )
        }

    }

}
