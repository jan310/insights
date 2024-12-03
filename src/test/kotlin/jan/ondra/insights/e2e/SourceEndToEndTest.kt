package jan.ondra.insights.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import jan.ondra.insights.models.Source
import jan.ondra.insights.persistence.SourceRowMapper
import jan.ondra.insights.util.USER_1_BEARER_TOKEN
import jan.ondra.insights.util.USER_1_ID
import jan.ondra.insights.util.USER_2_ID
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
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SourceEndToEndTest(@Autowired private val jdbcTemplate: JdbcTemplate) {

    private lateinit var source1: Source
    private lateinit var source2: Source

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
                    ('$USER_1_ID', 'user1@email.com', true, ARRAY[]::VARCHAR[]),
                    ('$USER_2_ID', 'user2@email.com', true, ARRAY[]::VARCHAR[]);
                    
                INSERT INTO sources
                    (user_id, name, description, isbn_13)
                VALUES
                    ('$USER_1_ID', 'name1', NULL, '0000000000000'),
                    ('$USER_1_ID', 'name2', 'description2', NULL),
                    ('$USER_2_ID', 'name3', 'description3', NULL);
            """.trimIndent()
        )

        val sources = jdbcTemplate.query("SELECT * FROM sources", SourceRowMapper()).apply { sortBy { it.id } }

        source1 = sources[0]
        source2 = sources[1]
    }

    @Test
    fun `create source`() {
        val generatedId = given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(
                """
                    {
                        "name": "Principles for Dealing with the Changing World Order: Why Nations Succeed and Fail",
                        "description": null,
                        "isbn13": "9781982160272"
                    }
                """.trimIndent()
            )
            .`when`()
            .post("/api/v1/sources")
            .then()
            .assertThat()
            .statusCode(CREATED.value())
            .extract()
            .path<Int>("id").toLong()

        assertThat(getSavedSource(generatedId)).isEqualTo(
            Source(
                id = generatedId,
                userId = USER_1_ID,
                name = "Principles for Dealing with the Changing World Order: Why Nations Succeed and Fail",
                description = null,
                isbn13 = "9781982160272"
            )
        )
    }

    @Test
    fun `get sources`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .`when`()
            .get("/api/v1/sources")
            .then()
            .assertThat()
            .statusCode(OK.value())
            .body(equalTo(ObjectMapper().writeValueAsString(listOf(source1, source2))))
    }

    @Test
    fun `update source`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(
                """
                    {
                        "name": "Principles for Dealing with the Changing World Order: Why Nations Succeed and Fail",
                        "description": "A book about principles for dealing with the changing world order.",
                        "isbn13": "9781982160272"
                    }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/sources/${source1.id}")
            .then()
            .assertThat()
            .statusCode(ACCEPTED.value())
            .body(equalTo(""))

        assertThat(getSavedSource(source1.id!!)).isEqualTo(
            Source(
                id = source1.id,
                userId = USER_1_ID,
                name = "Principles for Dealing with the Changing World Order: Why Nations Succeed and Fail",
                description = "A book about principles for dealing with the changing world order.",
                isbn13 = "9781982160272"
            )
        )
    }

    @Test
    fun `delete source`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .`when`()
            .delete("/api/v1/sources/${source1.id}")
            .then()
            .statusCode(NO_CONTENT.value())
            .body(equalTo(""))

        assertThat(getSavedSource(source1.id!!)).isNull()
    }

    private fun getSavedSource(id: Long): Source? {
        return jdbcTemplate.query("SELECT * FROM sources WHERE id = '$id'", SourceRowMapper()).firstOrNull()
    }

}
