package jan.ondra.insights.api

import jan.ondra.insights.models.FilterTag

data class IdDto(
    val id: Long
)

data class ErrorDto(
    val error: String
)

data class UserDto(
    val email: String,
    val notificationEnabled: Boolean,
    val notificationTime: Int,
    val notificationFilterTags: List<FilterTag>
)

data class SourceDto(
    val name: String,
    val description: String?,
    val isbn13: String?
)

data class InsightDto(
    val sourceId: Long?,
    val filterTags: List<FilterTag>,
    val note: String,
    val quote: String?
)
