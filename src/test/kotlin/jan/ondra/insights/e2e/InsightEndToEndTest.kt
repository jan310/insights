package jan.ondra.insights.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import jan.ondra.insights.models.FilterTag.PERSONAL_DEVELOPMENT
import jan.ondra.insights.models.Insight
import jan.ondra.insights.models.Source
import jan.ondra.insights.persistence.InsightRowMapper
import jan.ondra.insights.persistence.SourceRowMapper
import jan.ondra.insights.util.USER_1_BEARER_TOKEN
import jan.ondra.insights.util.USER_1_ID
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class InsightEndToEndTest(@Autowired private val jdbcTemplate: JdbcTemplate) {

    private lateinit var source1: Source
    private lateinit var source2: Source
    private lateinit var source3: Source

    private lateinit var insight1: Insight
    private lateinit var insight2: Insight
    private lateinit var insight3: Insight
    private lateinit var insight4: Insight

    @BeforeEach
    fun setup(@LocalServerPort port: Int) {
        RestAssured.reset()
        RestAssured.baseURI = "http://localhost:$port"

        jdbcTemplate.update(
            """
                DELETE FROM users;
                
                INSERT INTO users
                    (id, email, notification_enabled, notification_filter_tags)
                VALUES
                    ('1', 'user1@email.com', true, ARRAY[]::VARCHAR[]),
                    ('2', 'user2@email.com', true, ARRAY[]::VARCHAR[]);
                    
                INSERT INTO sources
                    (user_id, name, description, isbn_13)
                VALUES
                    ('1', 'name1', NULL, '0000000000000'),
                    ('1', 'name2', 'description2', NULL),
                    ('2', 'name3', 'description3', NULL);
            """.trimIndent()
        )

        val sources = jdbcTemplate.query("SELECT * FROM sources", SourceRowMapper()).apply { sortBy { it.id } }
        source1 = sources[0]
        source2 = sources[1]
        source3 = sources[2]

        jdbcTemplate.update(
            """
                INSERT INTO insights
                    (user_id, source_id, last_modified_date, filter_tags, note, quote)
                VALUES
                    (1, '${source1.id}', '2024-12-04', ARRAY[]::VARCHAR[], 'note1', 'quote1'),
                    (1, '${source2.id}', '2024-12-03', ARRAY[]::VARCHAR[], 'note2', 'quote2'),
                    (1, null, '2024-12-02', ARRAY[]::VARCHAR[], 'note3', 'quote3'),
                    (2, '${source3.id}', '2024-12-01', ARRAY[]::VARCHAR[], 'note4', 'quote4'),
                    (2, null, '2024-12-01', ARRAY[]::VARCHAR[], 'note4', 'quote4');
            """.trimIndent()
        )

        val insights = jdbcTemplate.query("SELECT * FROM insights", InsightRowMapper()).apply { sortBy { it.id } }
        insight1 = insights[0]
        insight2 = insights[1]
        insight3 = insights[2]
        insight4 = insights[3]
    }

    @Test
    fun `create insight`() {
        val generatedId = given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(
                """
                    {
                        "sourceId": "${source1.id}",
                        "filterTags": ["PERSONAL_DEVELOPMENT"],
                        "note": "A quote by Zig Ziglar. Success means putting in hard work.",
                        "quote": "There is no elevator to success. You have to take the stairs."
                    }
                """.trimIndent()
            )
            .`when`()
            .post("/api/v1/insights")
            .then()
            .assertThat()
            .statusCode(CREATED.value())
            .extract()
            .path<Int>("id").toLong()

        assertThat(getSavedInsight(generatedId)).isEqualTo(
            Insight(
                id = generatedId,
                userId = USER_1_ID,
                sourceId = source1.id,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(PERSONAL_DEVELOPMENT),
                note = "A quote by Zig Ziglar. Success means putting in hard work.",
                quote = "There is no elevator to success. You have to take the stairs."
            )
        )
    }

    @Test
    fun `get insights`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .`when`()
            .get("/api/v1/insights")
            .then()
            .assertThat()
            .statusCode(OK.value())
            .body(
                equalTo(
                    ObjectMapper().writeValueAsString(
                        listOf(
                            object {
                                val id = insight1.id
                                val userId = insight1.userId
                                val sourceId = insight1.sourceId
                                val lastModifiedDate = insight1.lastModifiedDate.toString()
                                val filterTags = insight1.filterTags
                                val note = insight1.note
                                val quote = insight1.quote
                            },
                            object {
                                val id = insight2.id
                                val userId = insight2.userId
                                val sourceId = insight2.sourceId
                                val lastModifiedDate = insight2.lastModifiedDate.toString()
                                val filterTags = insight2.filterTags
                                val note = insight2.note
                                val quote = insight2.quote
                            },
                            object {
                                val id = insight3.id
                                val userId = insight3.userId
                                val sourceId = insight3.sourceId
                                val lastModifiedDate = insight3.lastModifiedDate.toString()
                                val filterTags = insight3.filterTags
                                val note = insight3.note
                                val quote = insight3.quote
                            }
                        )
                    )
                )
            )
    }

    @Test
    fun `update insight`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(
                """
                    {
                        "sourceId": "${source1.id}",
                        "filterTags": ["PERSONAL_DEVELOPMENT"],
                        "note": "A quote by Zig Ziglar. Success means putting in hard work.",
                        "quote": "There is no elevator to success. You have to take the stairs."
                    }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/insights/${insight1.id}")
            .then()
            .assertThat()
            .statusCode(ACCEPTED.value())
            .body(equalTo(""))

        assertThat(getSavedInsight(insight1.id!!)).isEqualTo(
            Insight(
                id = insight1.id,
                userId = USER_1_ID,
                sourceId = source1.id,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(PERSONAL_DEVELOPMENT),
                note = "A quote by Zig Ziglar. Success means putting in hard work.",
                quote = "There is no elevator to success. You have to take the stairs."
            )
        )
    }

    @Test
    fun `delete insight`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .`when`()
            .delete("/api/v1/insights/${insight1.id}")
            .then()
            .statusCode(NO_CONTENT.value())
            .body(equalTo(""))

        assertThat(getSavedInsight(insight1.id!!)).isNull()
    }

    private fun getSavedInsight(id: Long): Insight? {
        return jdbcTemplate.query("SELECT * FROM insights WHERE id = $id", InsightRowMapper()).firstOrNull()
    }

}
