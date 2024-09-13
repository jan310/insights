package jan.ondra.insights.models

data class User(
    val id: String,
    val email: String
)

data class Insight(
    val id: Long,
    val userId: String,
    val content: String,
    val sourceType: SourceTypeEnum,
    val sourceInformation: SourceInformation
)

enum class SourceTypeEnum {
    BOOK,
    MOVIE
}

interface SourceInformation

data class BookSourceInformation(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val releaseYear: Int,
    val pages: Int,
) : SourceInformation

data class MovieSourceInformation(
    val imdbId: String,
    val name: String,
    val director: String,
    val releaseDate: String,
) : SourceInformation
