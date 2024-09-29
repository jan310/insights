package jan.ondra.insights.persistence

import jan.ondra.insights.models.FilterTag
import jan.ondra.insights.models.User
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class UserRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun create(user: User) {
        @Language("PostgreSQL")
        val sqlStatement = """
            INSERT INTO users
            (
                id,
                email,
                notification_enabled,
                notification_filter_tags
            )
            VALUES
            (
                :id,
                :email,
                :notification_enabled,
                ARRAY[:notification_filter_tags]::VARCHAR[]
            );
        """.trimIndent()

        val parameters = mapOf(
            "id" to user.id,
            "email" to user.email,
            "notification_enabled" to user.notificationEnabled,
            "notification_filter_tags" to user.notificationFilterTags.map { it.name },
        )

        jdbcTemplate.update(sqlStatement, parameters)
    }

    fun get(id: String): User? {
        @Language("PostgreSQL")
        val sqlStatement = "SELECT * FROM users WHERE id = :id;"

        val parameters = mapOf("id" to id)

        return jdbcTemplate.query(sqlStatement, parameters, UserRowMapper()).firstOrNull()
    }

    fun update(user: User): Int {
        @Language("PostgreSQL")
        val sqlStatement = """
            UPDATE users
            SET
                email = :email,
                notification_enabled = :notification_enabled,
                notification_filter_tags = ARRAY[:notification_filter_tags]::VARCHAR[]
            WHERE
                id = :id;
        """.trimIndent()

        val parameters = mapOf(
            "id" to user.id,
            "email" to user.email,
            "notification_enabled" to user.notificationEnabled,
            "notification_filter_tags" to user.notificationFilterTags.map { it.name },
        )

        return jdbcTemplate.update(sqlStatement, parameters)
    }

    fun delete(id: String): Int {
        @Language("PostgreSQL")
        val sqlStatement = "DELETE FROM users WHERE id = :id;"

        val parameters = mapOf("id" to id)

        return jdbcTemplate.update(sqlStatement, parameters)
    }

    private class UserRowMapper : RowMapper<User> {
        @Suppress("UNCHECKED_CAST")
        override fun mapRow(rs: ResultSet, rowNum: Int): User {
            return User(
                id = rs.getString("id"),
                email = rs.getString("email"),
                notificationEnabled = rs.getBoolean("notification_enabled"),
                notificationFilterTags = (rs.getArray("notification_filter_tags").array as Array<String>)
                    .map { enumValueOf<FilterTag>(it) }
            )
        }
    }

}
