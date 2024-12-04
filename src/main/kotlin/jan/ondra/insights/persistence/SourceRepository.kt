package jan.ondra.insights.persistence

import jan.ondra.insights.exception.SourceNotFoundException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.Source
import jan.ondra.insights.util.updateReturningKey
import org.intellij.lang.annotations.Language
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class SourceRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun createSource(source: Source): Long {
        @Language("PostgreSQL")
        val sqlStatement = """
            INSERT INTO sources
            (
                user_id,
                name,
                description,
                isbn_13
            )
            VALUES
            (
                :user_id,
                :name,
                :description,
                :isbn_13
            );
        """.trimIndent()

        val parameters = mapOf(
            "user_id" to source.userId,
            "name" to source.name,
            "description" to source.description,
            "isbn_13" to source.isbn13,
        )

        try {
            return jdbcTemplate.updateReturningKey(sqlStatement, parameters)
        } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
            throw UserNotRegisteredException(cause = dataIntegrityViolationException)
        }
    }

    fun getSources(userId: String): List<Source> {
        val sqlStatement = "SELECT * FROM sources WHERE user_id = :user_id;"

        val parameters = mapOf("user_id" to userId)

        return jdbcTemplate.query(sqlStatement, parameters, SourceRowMapper())
    }

    fun updateSource(source: Source) {
        val sqlStatement = """
            UPDATE sources
            SET
                name = :name,
                description = :description,
                isbn_13 = :isbn_13
            WHERE
                id = :id AND
                user_id = :user_id;
        """.trimIndent()

        val parameters = mapOf(
            "id" to source.id,
            "user_id" to source.userId,
            "name" to source.name,
            "description" to source.description,
            "isbn_13" to source.isbn13,
        )

        if (jdbcTemplate.update(sqlStatement, parameters) == 0) {
            throw SourceNotFoundException()
        }
    }

    fun deleteSource(id: Long, userId: String) {
        val sqlStatement = "DELETE FROM sources WHERE id = :id AND user_id = :user_id;"

        val parameters = mapOf(
            "id" to id,
            "user_id" to userId,
        )

        if (jdbcTemplate.update(sqlStatement, parameters) == 0) {
            throw SourceNotFoundException()
        }
    }

}

class SourceRowMapper : RowMapper<Source> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Source {
        return Source(
            id = rs.getLong("id"),
            userId = rs.getString("user_id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            isbn13 = rs.getString("isbn_13")
        )
    }
}
