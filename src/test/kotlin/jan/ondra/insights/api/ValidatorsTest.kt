package jan.ondra.insights.api

import jan.ondra.insights.api.ValidationConstants.MAX_SOURCE_DESCRIPTION_LENGTH
import jan.ondra.insights.api.ValidationConstants.MAX_SOURCE_NAME_LENGTH
import jan.ondra.insights.exception.InvalidRequestDataException
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
}
