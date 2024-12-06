package jan.ondra.insights.api

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
import jan.ondra.insights.util.USER_1_ID
import jan.ondra.insights.util.andDocument
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
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
@AutoConfigureRestDocs(outputDir = "build/generated-snippets/user")
class UserControllerMvcTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var userService: UserService

    @Nested
    inner class CreateUser {

        @Language("JSON")
        private val requestBody = """
            {
              "email": "user1@email.com",
              "notificationEnabled": false,
              "notificationFilterTags": ["PERSONAL_DEVELOPMENT", "WEALTH_CREATION"]
            }
        """.trimIndent()

        @Test
        fun `should return status code CREATED`() {
            justRun { userService.createUser(any()) }

            mockMvc
                .perform(post("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isCreated,
                    content().string("")
                )
                .andDocument("create-user-success")
        }

        @Test
        fun `should return status code CONFLICT and error message when user already exists`() {
            every { userService.createUser(any()) } throws UserAlreadyRegisteredException(DuplicateKeyException(null))

            mockMvc
                .perform(post("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isConflict,
                    content().json("""{"error": "Registration failed"}""")
                )
                .andDocument("create-user-error_user-already-registered")
        }

        @Test
        fun `should return status code CONFLICT and error message when email already exists`() {
            every { userService.createUser(any()) } throws EmailAlreadyExistsException(DuplicateKeyException(null))

            mockMvc
                .perform(post("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isConflict,
                    content().json("""{"error": "The email already exists"}""")
                )
                .andDocument("create-user-error_email-already-exists")
        }

    }

    @Nested
    inner class GetUser {

        @Test
        fun `should return status code OK`() {
            every { userService.getUser(any()) } returns User(
                id = USER_1_ID,
                email = "user1@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION)
            )

            mockMvc
                .perform(get("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isOk,
                    content().json(
                        """
                            {
                              "id": "$USER_1_ID",
                              "email": "user1@email.com",
                              "notificationEnabled": false,
                              "notificationFilterTags": ["PERSONAL_DEVELOPMENT", "WEALTH_CREATION"]
                            }
                        """.trimIndent()
                    )
                )
                .andDocument("get-user-success")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when user does not exist`() {
            every { userService.getUser(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(get("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "User is not registered"}""")
                )
                .andDocument("get-user-error_user-not-registered")
        }

    }

    @Nested
    inner class UpdateUser {

        @Language("JSON")
        private val requestBody = """
            {
              "email": "user1@email.com",
              "notificationEnabled": false,
              "notificationFilterTags": ["PERSONAL_DEVELOPMENT", "WEALTH_CREATION"]
            }
        """.trimIndent()

        @Test
        fun `should return status code NO_CONTENT`() {
            justRun { userService.updateUser(any()) }

            mockMvc
                .perform(put("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNoContent,
                    content().string("")
                )
                .andDocument("update-user-success")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when user does not exist`() {
            every { userService.updateUser(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(put("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "User is not registered"}""")
                )
                .andDocument("update-user-error_user-not-registered")
        }

        @Test
        fun `should return status code CONFLICT and error message when email already exists`() {
            every { userService.updateUser(any()) } throws EmailAlreadyExistsException(DuplicateKeyException(null))

            mockMvc
                .perform(put("/api/v1/users")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isConflict,
                    content().json("""{"error": "The email already exists"}""")
                )
                .andDocument("update-user-error_email-already-exists")
        }

    }

    @Nested
    inner class DeleteUser {

        @Test
        fun `should return status code NO_CONTENT`() {
            justRun { userService.deleteUser(any()) }

            mockMvc
                .perform(delete("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNoContent,
                    content().string("")
                )
                .andDocument("delete-user-success")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when user does not exist`() {
            every { userService.deleteUser(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(delete("/api/v1/users")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "User is not registered"}""")
                )
                .andDocument("delete-user-error_user-not-registered")
        }

    }

}
