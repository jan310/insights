package jan.ondra.insights.persistence

import jan.ondra.insights.models.User
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun create(user: User) {
        jdbcTemplate.update(
            "INSERT INTO users (id, email) VALUES (:id, :email);",
            mapOf(
                "id" to user.id,
                "email" to user.email,
            )
        )
    }

    fun update(user: User): Int {
        return jdbcTemplate.update(
            "UPDATE users SET email = :email WHERE id = :id;",
            mapOf(
                "id" to user.id,
                "email" to user.email,
            )
        )
    }

    fun delete(id: String): Int {
        return jdbcTemplate.update(
            "DELETE FROM users WHERE id = :id;",
            mapOf("id" to id),
        )
    }

}
