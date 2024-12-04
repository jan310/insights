package jan.ondra.insights.persistence

import jan.ondra.insights.exception.InsightNotFoundException
import jan.ondra.insights.exception.SourceDoesNotBelongToUserException
import jan.ondra.insights.exception.SourceNotFoundException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.FilterTag
import jan.ondra.insights.models.Insight
import jan.ondra.insights.util.updateReturningKey
import org.intellij.lang.annotations.Language
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class InsightRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun createInsight(insight: Insight): Long {
        if (insight.sourceId != null && !sourceBelongsToUser(insight.sourceId, insight.userId)) {
            throw SourceDoesNotBelongToUserException()
        }

        @Language("PostgreSQL")
        val sqlStatement = """
            INSERT INTO insights
            (
                user_id,
                source_id,
                last_modified_date,
                filter_tags,
                note,
                quote
            )
            VALUES
            (
                :user_id,
                :source_id,
                :last_modified_date,
                ARRAY[:filter_tags]::VARCHAR[],
                :note,
                :quote
            );
        """.trimIndent()

        val parameters = mapOf(
            "user_id" to insight.userId,
            "source_id" to insight.sourceId,
            "last_modified_date" to insight.lastModifiedDate,
            "filter_tags" to insight.filterTags.map { it.name },
            "note" to insight.note,
            "quote" to insight.quote
        )

        try {
            return jdbcTemplate.updateReturningKey(sqlStatement, parameters)
        } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
            throw UserNotRegisteredException(cause = dataIntegrityViolationException)
        }
    }

    fun getInsights(userId: String): List<Insight> {
        val sqlStatement = "SELECT * FROM insights WHERE user_id = :user_id;"

        val parameters = mapOf("user_id" to userId)

        return jdbcTemplate.query(sqlStatement, parameters, InsightRowMapper())
    }

    fun updateInsight(insight: Insight) {
        if (insight.sourceId != null && !sourceBelongsToUser(insight.sourceId, insight.userId)) {
            throw SourceDoesNotBelongToUserException()
        }

        val sqlStatement = """
            UPDATE insights
            SET
                source_id = :source_id,
                last_modified_date = :last_modified_date,
                filter_tags = ARRAY[:filter_tags]::VARCHAR[],
                note = :note,
                quote = :quote
            WHERE
                id = :id AND
                user_id = :user_id;
        """.trimIndent()

        val parameters = mapOf(
            "id" to insight.id,
            "user_id" to insight.userId,
            "source_id" to insight.sourceId,
            "last_modified_date" to insight.lastModifiedDate,
            "filter_tags" to insight.filterTags.map { it.name },
            "note" to insight.note,
            "quote" to insight.quote
        )

        if (jdbcTemplate.update(sqlStatement, parameters) == 0) {
            throw InsightNotFoundException()
        }
    }

    fun deleteInsight(id: Long, userId: String) {
        val sqlStatement = "DELETE FROM insights WHERE id = :id AND user_id = :user_id;"

        val parameters = mapOf(
            "id" to id,
            "user_id" to userId,
        )

        if (jdbcTemplate.update(sqlStatement, parameters) == 0) {
            throw InsightNotFoundException()
        }
    }

    private fun sourceBelongsToUser(sourceId: Long, userId: String): Boolean {
        val sqlStatement = "SELECT user_id FROM sources WHERE id = :source_id;"
        val parameters = mapOf("source_id" to sourceId)

        try {
            return jdbcTemplate.queryForObject(sqlStatement, parameters, String::class.java) == userId
        } catch (emptyResultDataAccessException: EmptyResultDataAccessException) {
            throw SourceNotFoundException(cause = emptyResultDataAccessException)
        }
    }

}

class InsightRowMapper : RowMapper<Insight> {
    @Suppress("UNCHECKED_CAST")
    override fun mapRow(rs: ResultSet, rowNum: Int): Insight {
        return Insight(
            id = rs.getLong("id"),
            userId = rs.getString("user_id"),
            sourceId = rs.getLong("source_id").let {
                if (rs.wasNull()) null else it
            },
            lastModifiedDate = rs.getDate("last_modified_date").toLocalDate(),
            filterTags = (rs.getArray("filter_tags").array as Array<String>)
                .map { enumValueOf<FilterTag>(it) },
            note = rs.getString("note"),
            quote = rs.getString("quote"),
        )
    }
}
