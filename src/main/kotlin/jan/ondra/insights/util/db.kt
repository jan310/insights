package jan.ondra.insights.util

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder

fun NamedParameterJdbcTemplate.updateReturningKey(sql: String, paramMap: Map<String, Any?>): Long {
    return GeneratedKeyHolder().let {
        update(sql, MapSqlParameterSource(paramMap), it)
        it.keyList.first()["id"] as Long
    }
}
