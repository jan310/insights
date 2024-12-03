package jan.ondra.insights.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import jan.ondra.insights.business.SourceService
import jan.ondra.insights.exception.SourceNotFoundException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.Source
import jan.ondra.insights.util.USER_1_BEARER_TOKEN
import jan.ondra.insights.util.USER_1_ID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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
@WebMvcTest(SourceController::class)
@AutoConfigureMockMvc(addFilters = false)
class SourceControllerMvcTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var sourceService: SourceService

    @Nested
    inner class CreateSource {

        @Language("JSON")
        private val requestBody = """
            {
              "name": "Principles for Dealing with the Changing World Order: Why Nations Succeed and Fail",
              "description": null,
              "isbn13": "9781982160272"
            }
        """.trimIndent()

        @Test
        fun `should return status code CREATED and ID of created source`() {
            every { sourceService.createSource(any()) } returns 1

            mockMvc
                .perform(post("/api/v1/sources")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isCreated,
                    content().json("""{"id":1}""")
                )
        }

        @Test
        fun `should return status code NOT_FOUND and error message when user does not exist`() {
            every { sourceService.createSource(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(post("/api/v1/sources")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "User is not registered"}""")
                )
        }

    }

    @Nested
    inner class GetSources {

        @Test
        fun `should return status code OK and list of sources`() {
            every { sourceService.getSources(any()) } returns listOf(
                Source(id = 1, userId = USER_1_ID, name = "name1", description = null, isbn13 = "9781982160272"),
                Source(id = 2, userId = USER_1_ID, name = "name2", description = "description2", isbn13 = null)
            )

            mockMvc
                .perform(get("/api/v1/sources")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isOk,
                    content().json(
                        """
                            [
                              {
                                "id": 1,
                                "userId":"$USER_1_ID",
                                "name": "name1",
                                "description": null,
                                "isbn13": "9781982160272"
                              },
                              {
                                "id": 2,
                                "userId":"$USER_1_ID",
                                "name": "name2",
                                "description":"description2",
                                "isbn13": null               
                              }
                            ]
                        """.trimIndent()
                    )
                )
        }

    }

    @Nested
    inner class UpdateSource {

        @Language("JSON")
        private val requestBody = """
            {
              "name": "Principles for Dealing with the Changing World Order: Why Nations Succeed and Fail",
              "description": null,
              "isbn13": "9781982160272"
            }
        """.trimIndent()

        @Test
        fun `should return status code ACCEPTED`() {
            justRun { sourceService.updateSource(any()) }

            mockMvc
                .perform(put("/api/v1/sources/1")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isAccepted,
                    content().string("")
                )
        }

        @Test
        fun `should return status code NOT_FOUND and error message when source does not exist`() {
            every { sourceService.updateSource(any()) } throws SourceNotFoundException()

            mockMvc
                .perform(put("/api/v1/sources/1")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Source not found"}""")
                )
        }

    }

    @Nested
    inner class DeleteSource {

        @Test
        fun `should return status code NO_CONTENT`() {
            justRun { sourceService.deleteSource(any(), any()) }

            mockMvc
                .perform(delete("/api/v1/sources/1")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNoContent,
                    content().string("")
                )
        }

        @Test
        fun `should return status code NOT_FOUND and error message when source does not exist`() {
            every { sourceService.deleteSource(any(), any()) } throws SourceNotFoundException()

            mockMvc
                .perform(delete("/api/v1/sources/1")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Source not found"}""")
                )
        }

    }

}
