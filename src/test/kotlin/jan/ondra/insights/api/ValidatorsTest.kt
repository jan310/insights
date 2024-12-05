package jan.ondra.insights.api

import jan.ondra.insights.api.ValidationConstants.MAX_INSIGHT_NOTE_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_INSIGHT_QUOTE_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_SOURCE_DESCRIPTION_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_SOURCE_NAME_LENGTH
import jan.ondra.insights.exception.InvalidRequestDataException
import jan.ondra.insights.models.FilterTag.PERSONAL_DEVELOPMENT
import jan.ondra.insights.models.FilterTag.WEALTH_CREATION
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ValidatorsTest {

    private fun createStringWithLength(length: Int): String {
        return "A".repeat(length)
    }

    @Nested
    inner class ValidateUserDto {

        private val userDto = UserDto(
            email = "",
            notificationEnabled = true,
            notificationFilterTags = emptyList()
        )

        @TestFactory
        fun `valid DTOs`(): List<DynamicTest> {
            return listOf(
                userDto.copy(email = "email@example.com"),
                userDto.copy(email = "_______@example.com"),
                userDto.copy(email = "email@123.123.123.123")
            ).map { userDto ->
                dynamicTest("UserDto with email '${userDto.email}' is valid") {
                    assertDoesNotThrow { userDto.validate() }
                }
            }
        }

        @TestFactory
        fun `invalid DTOs`(): List<DynamicTest> {
            return listOf(
                userDto.copy(email = "あいうえお@example.com"),
                userDto.copy(email = "#@%^%#@#@#.com"),
                userDto.copy(email = ""),
            ).map { userDto ->
                dynamicTest("UserDto with email '${userDto.email}' is invalid") {
                    assertThrows<InvalidRequestDataException> { userDto.validate() }
                }
            }
        }
    }

    @Nested
    inner class ValidateSourceDto {

        @Test
        fun `should not fail when DTO is valid`() {
            assertDoesNotThrow {
                SourceDto(
                    name = createStringWithLength(MAX_SOURCE_NAME_LENGTH),
                    description = createStringWithLength(MAX_SOURCE_DESCRIPTION_LENGTH),
                    isbn13 = "9781982160272"
                ).validate()
            }
        }

        @Test
        fun `should fail when name is too long`() {
            assertThrows<InvalidRequestDataException> {
                SourceDto(
                    name = createStringWithLength(MAX_SOURCE_NAME_LENGTH + 1),
                    description = "description",
                    isbn13 = "9781982160272"
                ).validate()
            }
        }

        @Test
        fun `should fail when description is too long`() {
            assertThrows<InvalidRequestDataException> {
                SourceDto(
                    name = "name",
                    description = createStringWithLength(MAX_SOURCE_DESCRIPTION_LENGTH + 1),
                    isbn13 = "9781982160272"
                ).validate()
            }
        }

        @Test
        fun `should fail when isbn13 isn't 13 characters long`() {
            assertThrows<InvalidRequestDataException> {
                SourceDto(
                    name = "name",
                    description = "description",
                    isbn13 = "97819821602722"
                ).validate()
            }
        }
    }

    @Nested
    inner class ValidateInsightDto {

        @Test
        fun `should not fail when DTO is valid`() {
            assertDoesNotThrow {
                InsightDto(
                    sourceId = 1,
                    filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                    note = createStringWithLength(MAX_INSIGHT_NOTE_LENGTH),
                    quote = createStringWithLength(MAX_INSIGHT_QUOTE_LENGTH)
                ).validate()
            }

            assertDoesNotThrow {
                InsightDto(
                    sourceId = null,
                    filterTags = listOf(),
                    note = "",
                    quote = null
                ).validate()
            }
        }

        @Test
        fun `should fail when note is too long`() {
            assertThrows<InvalidRequestDataException> {
                InsightDto(
                    sourceId = 1,
                    filterTags = listOf(),
                    note = createStringWithLength(MAX_INSIGHT_NOTE_LENGTH + 1),
                    quote = "quote"
                ).validate()
            }
        }

        @Test
        fun `should fail when quote is too long`() {
            assertThrows<InvalidRequestDataException> {
                InsightDto(
                    sourceId = 1,
                    filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                    note = "note",
                    quote = createStringWithLength(MAX_INSIGHT_QUOTE_LENGTH + 1)
                ).validate()
            }
        }

    }

}
