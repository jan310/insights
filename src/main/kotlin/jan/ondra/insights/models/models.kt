package jan.ondra.insights.models

import java.time.LocalDate

data class User(
    val id: String,
    val email: String,
    val notificationEnabled: Boolean,
    val notificationTime: Int,
    val notificationFilterTags: List<FilterTag>,
)

data class Source(
    val id: Long? = null,
    val userId: String,
    val name: String,
    val description: String?,
    val isbn13: String?
)

data class Insight(
    val id: Long? = null,
    val userId: String,
    val sourceId: Long?,
    val lastModifiedDate: LocalDate,
    val filterTags: List<FilterTag>,
    val note: String,
    val quote: String?
)

enum class FilterTag {
    PERSONAL_DEVELOPMENT,
    WEALTH_CREATION
}
