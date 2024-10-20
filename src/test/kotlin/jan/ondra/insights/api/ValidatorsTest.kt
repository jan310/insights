package jan.ondra.insights.api

import jan.ondra.insights.exception.InvalidRequestDataException
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ValidatorsTest {

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
}
