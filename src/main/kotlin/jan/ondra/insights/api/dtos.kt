package jan.ondra.insights.api

import jan.ondra.insights.models.FilterTag

data class UserDto(
    val email: String,
    val notificationEnabled: Boolean,
    val notificationFilterTags: List<FilterTag>
)

data class SourceDto(
    val name: String,
    val description: String?,
    val isbn13: String?
)

data class InsightDto(
    val filterTags: List<FilterTag>,
    val note: String,
    val quote: String?
)
