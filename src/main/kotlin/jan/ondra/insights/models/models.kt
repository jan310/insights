package jan.ondra.insights.models

import java.time.LocalDate

data class User(
    val id: String,
    val email: String,
    val notificationEnabled: Boolean,
    val notificationFilterTags: List<FilterTag>,
)

data class Source(
    val id: Long,
    val userId: String,
    val name: String,
    val description: String?,
    val isbn13: String?
)

data class Insight(
    val id: Long,
    val sourceId: Long,
    val lastModifiedDate: LocalDate,
    val content: String,
    val filterTags: List<FilterTag>,
)

enum class FilterTag {
    PERSONAL_DEVELOPMENT,
    WEALTH_CREATION
}
