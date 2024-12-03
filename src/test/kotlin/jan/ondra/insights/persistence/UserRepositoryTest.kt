package jan.ondra.insights.persistence

import jan.ondra.insights.exception.EmailAlreadyExistsException
import jan.ondra.insights.exception.UserAlreadyRegisteredException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.FilterTag.PERSONAL_DEVELOPMENT
import jan.ondra.insights.models.FilterTag.WEALTH_CREATION
import jan.ondra.insights.models.User
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@JdbcTest
@Import(UserRepository::class)
@AutoConfigureTestDatabase(replace = NONE)
class UserRepositoryTest(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setup() {
        jdbcTemplate.update(
            """
                DELETE FROM users;
                
                INSERT INTO users
                    (id, email, notification_enabled, notification_filter_tags)
                VALUES
                    ('1', 'user1@email.com', true, ARRAY['PERSONAL_DEVELOPMENT']::VARCHAR[]),
                    ('2', 'user2@email.com', true, ARRAY[]::VARCHAR[]);
            """.trimIndent()
        )
    }

    @Nested
    inner class CreateUser {

        @Test
        fun `should create user`() {
            val user = User(
                id = "3",
                email = "user3@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(PERSONAL_DEVELOPMENT, WEALTH_CREATION),
            )

            userRepository.createUser(user)

            assertThat(getSavedUser(user.id)).isEqualTo(user)
        }

        @Test
        fun `should create user with empty tag list`() {
            val user = User(
                id = "3",
                email = "user3@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(),
            )

            userRepository.createUser(user)

            assertThat(getSavedUser(user.id)).isEqualTo(user)
        }

        @Test
        fun `should fail when user ID already exists`() {
            val user = User(
                id = "1",
                email = "user3@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(),
            )

            assertThatThrownBy { userRepository.createUser(user) }
                .isInstanceOf(UserAlreadyRegisteredException::class.java)
                .hasCauseInstanceOf(DuplicateKeyException::class.java)
                .cause().message().contains("duplicate key value violates unique constraint \"users_pkey\"")
        }

        @Test
        fun `should fail when email already exists`() {
            val user = User(
                id = "3",
                email = "user1@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(),
            )

            assertThatThrownBy { userRepository.createUser(user) }
                .isInstanceOf(EmailAlreadyExistsException::class.java)
                .hasCauseInstanceOf(DuplicateKeyException::class.java)
                .cause().message().contains("duplicate key value violates unique constraint \"users_email_key\"")
        }

    }

    @Nested
    inner class GetUser {

        @Test
        fun `should return user`() {
            assertThat(userRepository.getUser("1")).isEqualTo(
                User(
                    id = "1",
                    email = "user1@email.com",
                    notificationEnabled = true,
                    notificationFilterTags = listOf(PERSONAL_DEVELOPMENT),
                )
            )
        }

        @Test
        fun `should fail when user does not exist`() {
            assertThatThrownBy { userRepository.getUser("3") }
                .isInstanceOf(UserNotRegisteredException::class.java)
        }

    }

    @Nested
    inner class UpdateUser {

        @Test
        fun `should update user`() {
            val user = User(
                id = "1",
                email = "new@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(WEALTH_CREATION),
            )

            userRepository.updateUser(user)

            assertThat(getSavedUser(user.id)).isEqualTo(user)
        }

        @Test
        fun `should fail when user does not exist`() {
            val user = User(
                id = "3",
                email = "new@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(WEALTH_CREATION),
            )

            assertThatThrownBy { userRepository.updateUser(user) }.isInstanceOf(UserNotRegisteredException::class.java)
        }

        @Test
        fun `should fail when email already exists`() {
            val user = User(
                id = "1",
                email = "user2@email.com",
                notificationEnabled = false,
                notificationFilterTags = listOf(WEALTH_CREATION),
            )

            assertThatThrownBy { userRepository.updateUser(user) }
                .isInstanceOf(EmailAlreadyExistsException::class.java)
                .hasCauseInstanceOf(DuplicateKeyException::class.java)
                .cause().message().contains("duplicate key value violates unique constraint \"users_email_key\"")
        }

    }

    @Nested
    inner class DeleteUser {

        @Test
        fun `should delete user`() {
            userRepository.deleteUser("2")

            assertThat(getSavedUser("2")).isNull()
        }

        @Test
        fun `should fail when user does not exist`() {
            assertThatThrownBy { userRepository.deleteUser("3") }
                .isInstanceOf(UserNotRegisteredException::class.java)
        }

    }

    private fun getSavedUser(id: String): User? {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = '$id'", UserRowMapper()).firstOrNull()
    }

}
