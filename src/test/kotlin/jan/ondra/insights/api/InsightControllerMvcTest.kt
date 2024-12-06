package jan.ondra.insights.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import jan.ondra.insights.business.InsightService
import jan.ondra.insights.exception.InsightNotFoundException
import jan.ondra.insights.exception.SourceDoesNotBelongToUserException
import jan.ondra.insights.exception.SourceNotFoundException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.Insight
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
import java.time.LocalDate

@ActiveProfiles("test")
@WebMvcTest(InsightController::class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets/insight")
class InsightControllerMvcTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var insightService: InsightService

    @Nested
    inner class CreateInsight {

        @Language("JSON")
        private val requestBody = """
            {
              "sourceId": 1,
              "filterTags": [],
              "note": "The money is always right!",
              "quote": null
            }
        """.trimIndent()

        @Test
        fun `should return status code CREATED and ID of created insight`() {
            every { insightService.createInsight(any()) } returns 1

            mockMvc
                .perform(
                    post("/api/v1/insights")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isCreated,
                    content().json("""{"id":1}""")
                )
                .andDocument("create-insight-success")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when user does not exist`() {
            every { insightService.createInsight(any()) } throws UserNotRegisteredException()

            mockMvc
                .perform(post("/api/v1/insights")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "User is not registered"}""")
                )
                .andDocument("create-insight-error_user-not-registered")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when source does not exist`() {
            every { insightService.createInsight(any()) } throws SourceNotFoundException()

            mockMvc
                .perform(post("/api/v1/insights")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Source not found"}""")
                )
                .andDocument("create-insight-error_source-not-found")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when source does not belong to the user`() {
            every { insightService.createInsight(any()) } throws SourceDoesNotBelongToUserException()

            mockMvc
                .perform(post("/api/v1/insights")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Source not found"}""")
                )
                .andDocument("create-insight-error_source-does-not-belong-to-user")
        }

    }

    @Nested
    inner class GetInsights {

        @Test
        fun `should return status code OK and list of insights`() {
            every { insightService.getInsights(any()) } returns listOf(
                Insight(
                    id = 1,
                    userId = USER_1_ID,
                    sourceId = 1,
                    lastModifiedDate = LocalDate.of(2024, 12, 4),
                    filterTags = listOf(),
                    note = "The money is always right!",
                    quote = null
                ),
                Insight(
                    id = 2,
                    userId = USER_1_ID,
                    sourceId = 1,
                    lastModifiedDate = LocalDate.of(2024, 12, 4),
                    filterTags = listOf(),
                    note = "The money is always right!",
                    quote = null
                )
            )

            mockMvc
                .perform(
                    get("/api/v1/insights")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isOk,
                    content().json(
                        """
                            [
                              {
                                "id": 1,
                                "userId": "$USER_1_ID",
                                "sourceId": 1,
                                "lastModifiedDate": "2024-12-04",
                                "filterTags": [],
                                "note": "The money is always right!"
                              },
                              {
                                "id": 2,
                                "userId": "$USER_1_ID",
                                "sourceId": 1,
                                "lastModifiedDate": "2024-12-04",
                                "filterTags": [],
                                "note": "The money is always right!"              
                              }
                            ]
                        """.trimIndent()
                    )
                )
                .andDocument("get-insights-success")
        }

    }

    @Nested
    inner class UpdateInsight {

        @Language("JSON")
        private val requestBody = """
            {
              "sourceId": 1,
              "filterTags": [],
              "note": "The money is always right!",
              "quote": null
            }
        """.trimIndent()

        @Test
        fun `should return status code ACCEPTED`() {
            justRun { insightService.updateInsight(any()) }

            mockMvc
                .perform(
                    put("/api/v1/insights/1")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isAccepted,
                    content().string("")
                )
                .andDocument("update-insight-success")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when insight does not exist`() {
            every { insightService.updateInsight(any()) } throws InsightNotFoundException()

            mockMvc
                .perform(put("/api/v1/insights/1")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Insight not found"}""")
                )
                .andDocument("update-insight-error_insight-not-found")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when source does not exist`() {
            every { insightService.updateInsight(any()) } throws SourceNotFoundException()

            mockMvc
                .perform(put("/api/v1/insights/1")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Source not found"}""")
                )
                .andDocument("update-insight-error_source-not-found")
        }

        @Test
        fun `should return status code NOT_FOUND and error when source does not belong to the user`() {
            every { insightService.updateInsight(any()) } throws SourceDoesNotBelongToUserException()

            mockMvc
                .perform(put("/api/v1/insights/1")
                    .contentType(APPLICATION_JSON_VALUE)
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                    .content(requestBody)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Source not found"}""")
                )
                .andDocument("update-insight-error_source-does-not-belong-to-user")
        }

    }

    @Nested
    inner class DeleteInsight {

        @Test
        fun `should return status code NO_CONTENT`() {
            justRun { insightService.deleteInsight(any(), any()) }

            mockMvc
                .perform(
                    delete("/api/v1/insights/1")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNoContent,
                    content().string("")
                )
                .andDocument("delete-insight-success")
        }

        @Test
        fun `should return status code NOT_FOUND and error message when insight does not exist`() {
            every { insightService.deleteInsight(any(), any()) } throws InsightNotFoundException()

            mockMvc
                .perform(delete("/api/v1/insights/1")
                    .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
                )
                .andExpectAll(
                    status().isNotFound,
                    content().json("""{"error": "Insight not found"}""")
                )
                .andDocument("delete-insight-error_insight-not-found")
        }

    }

}
