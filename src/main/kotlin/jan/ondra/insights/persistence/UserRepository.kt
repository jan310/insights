package jan.ondra.insights.persistence

import jan.ondra.insights.exception.EmailAlreadyExistsException
import jan.ondra.insights.exception.UserAlreadyRegisteredException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.FilterTag
import jan.ondra.insights.models.User
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

private const val DUPLICATE_EMAIL_VIOLATION = "duplicate key value violates unique constraint \"users_email_key\""
private const val DUPLICATE_ID_VIOLATION = "duplicate key value violates unique constraint \"users_pkey\""

@Repository
class UserRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun create(user: User) {
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

        try {
            jdbcTemplate.update(sqlStatement, parameters)
        } catch (duplicateKeyException: DuplicateKeyException) {
            duplicateKeyException.message?.let { message ->
                throw if (message.contains(DUPLICATE_EMAIL_VIOLATION)) {
                    EmailAlreadyExistsException(cause = duplicateKeyException)
                } else if (message.contains(DUPLICATE_ID_VIOLATION)) {
                    UserAlreadyRegisteredException(cause = duplicateKeyException)
                } else {
                    duplicateKeyException
                }
            } ?: throw duplicateKeyException
        }
    }

    fun get(id: String): User {
        val sqlStatement = "SELECT * FROM users WHERE id = :id;"

        val parameters = mapOf("id" to id)

        return jdbcTemplate.query(sqlStatement, parameters, UserRowMapper()).firstOrNull()
            ?: throw UserNotRegisteredException()
    }

    fun update(user: User) {
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

        try {
            if (jdbcTemplate.update(sqlStatement, parameters) == 0) {
                throw UserNotRegisteredException()
            }
        } catch (duplicateKeyException: DuplicateKeyException) {
            throw EmailAlreadyExistsException(cause = duplicateKeyException)
        }
    }

    fun delete(id: String) {
        val sqlStatement = "DELETE FROM users WHERE id = :id;"

        val parameters = mapOf("id" to id)

        if (jdbcTemplate.update(sqlStatement, parameters) == 0) {
            throw UserNotRegisteredException()
        }
    }

}

class UserRowMapper : RowMapper<User> {
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
