package jan.ondra.insights.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import jan.ondra.insights.api.UserDto
import jan.ondra.insights.models.FilterTag.PERSONAL_DEVELOPMENT
import jan.ondra.insights.models.FilterTag.WEALTH_CREATION
import jan.ondra.insights.models.User
import jan.ondra.insights.persistence.UserRowMapper
import jan.ondra.insights.util.USER_1_BEARER_TOKEN
import jan.ondra.insights.util.USER_1_EMAIL
import jan.ondra.insights.util.USER_1_ID
import jan.ondra.insights.util.USER_2_BEARER_TOKEN
import jan.ondra.insights.util.USER_2_EMAIL
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
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class UserEndToEndTest(@Autowired private val jdbcTemplate: JdbcTemplate) {

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
                    ('$USER_1_ID', '$USER_1_EMAIL', true, ARRAY['PERSONAL_DEVELOPMENT']::VARCHAR[]);
            """.trimIndent()
        )
    }

    @Test
    fun `create user`() {
        given()
            .header(AUTHORIZATION, USER_2_BEARER_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(
                ObjectMapper().writeValueAsString(
                    UserDto(
                        email = USER_2_EMAIL,
                        notificationEnabled = false,
                        notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
                    )
                )
            )
            .`when`()
            .post("/api/v1/users")
            .then()
            .assertThat()
            .statusCode(CREATED.value())
            .body(equalTo(""))

        assertThat(getSavedUser(USER_2_ID)).isEqualTo(
            User(
                id = USER_2_ID,
                email = USER_2_EMAIL,
                notificationEnabled = false,
                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
            )
        )
    }

    @Test
    fun `get user`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .`when`()
            .get("/api/v1/users")
            .then()
            .assertThat()
            .statusCode(OK.value())
            .body(
                equalTo(
                    ObjectMapper().writeValueAsString(
                        User(
                            id = USER_1_ID,
                            email = USER_1_EMAIL,
                            notificationEnabled = true,
                            notificationFilterTags = listOf(PERSONAL_DEVELOPMENT)
                        )
                    )
                )
            )
    }

    @Test
    fun `update user`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(
                ObjectMapper().writeValueAsString(
                    UserDto(
                        email = "new@email.com",
                        notificationEnabled = true,
                        notificationFilterTags = listOf(WEALTH_CREATION),
                    )
                )
            )
            .`when`()
            .put("/api/v1/users")
            .then()
            .assertThat()
            .statusCode(NO_CONTENT.value())
            .body(equalTo(""))

        assertThat(getSavedUser(USER_1_ID)).isEqualTo(
            User(
                id = USER_1_ID,
                email = "new@email.com",
                notificationEnabled = true,
                notificationFilterTags = listOf(WEALTH_CREATION),
            )
        )
    }

    @Test
    fun `delete user`() {
        given()
            .header(AUTHORIZATION, USER_1_BEARER_TOKEN)
            .`when`()
            .delete("/api/v1/users")
            .then()
            .statusCode(NO_CONTENT.value())
            .body(equalTo(""))

        assertThat(getSavedUser(USER_1_ID)).isNull()
    }

    private fun getSavedUser(id: String): User? {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = '${id}'", UserRowMapper()).firstOrNull()
    }

}
