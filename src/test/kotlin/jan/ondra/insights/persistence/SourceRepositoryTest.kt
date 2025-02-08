package jan.ondra.insights.persistence

import jan.ondra.insights.exception.SourceNotFoundException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.Source
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
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@JdbcTest
@Import(SourceRepository::class)
@AutoConfigureTestDatabase(replace = NONE)
class SourceRepositoryTest(
    @Autowired private val sourceRepository: SourceRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate
) {

    private lateinit var source1: Source
    private lateinit var source2: Source

    @BeforeEach
    fun setup() {
        jdbcTemplate.update(
            """
                DELETE FROM users; 
                
                INSERT INTO users
                    (id, email, notification_enabled, notification_time, notification_filter_tags)
                VALUES
                    ('1', 'user1@email.com', true, 6, ARRAY[]::VARCHAR[]),
                    ('2', 'user2@email.com', true, 6, ARRAY[]::VARCHAR[]);
                    
                INSERT INTO sources
                    (user_id, name, description, isbn_13)
                VALUES
                    ('1', 'name1', NULL, '0000000000000'),
                    ('1', 'name2', 'description2', NULL),
                    ('2', 'name3', 'description3', NULL);
            """.trimIndent()
        )

        val sources = jdbcTemplate.query("SELECT * FROM sources", SourceRowMapper()).apply { sortBy { it.id } }
        source1 = sources[0]
        source2 = sources[1]
    }

    @Nested
    inner class CreateSource {

        @Test
        fun `should create source`() {
            val source = Source(
                userId = "2",
                name = "name",
                description = "description",
                isbn13 = null
            )

            val sourceId = sourceRepository.createSource(source)

            assertThat(getSavedSource(sourceId)).isEqualTo(source.copy(id = sourceId))
        }

        @Test
        fun `should fail when user does not exist`() {
            val source = Source(
                userId = "3",
                name = "name",
                description = "description",
                isbn13 = null
            )

            assertThatThrownBy { sourceRepository.createSource(source) }
                .isInstanceOf(UserNotRegisteredException::class.java)
                .hasCauseInstanceOf(DataIntegrityViolationException::class.java)
                .cause().message().contains(
                    "insert or update on table \"sources\" violates foreign key constraint \"sources_user_id_fkey\""
                )
        }

    }

    @Nested
    inner class GetSources {

        @Test
        fun `should get all sources from the user`() {
            assertThat(sourceRepository.getSources("1")).containsExactlyInAnyOrder(source1, source2)
        }

    }

    @Nested
    inner class UpdateSource {

        @Test
        fun `should update the source`() {
            val updatedSource = Source(
                id = source1.id,
                userId = "1",
                name = "new name",
                description = "new description",
                isbn13 = null
            )

            sourceRepository.updateSource(updatedSource)

            assertThat(getSavedSource(source1.id!!)).isEqualTo(updatedSource)
        }

        @Test
        fun `should fail when source does not exist`() {
            assertThatThrownBy {
                sourceRepository.updateSource(
                    Source(
                        id = 999999999,
                        userId = "1",
                        name = "new name",
                        description = "new description",
                        isbn13 = null
                    )
                )
            }.isInstanceOf(SourceNotFoundException::class.java)
        }

        @Test
        fun `should fail when source does not belong to the user`() {
            assertThatThrownBy {
                sourceRepository.updateSource(
                    Source(
                        id = source1.id,
                        userId = "2",
                        name = "new name",
                        description = "new description",
                        isbn13 = null
                    )
                )
            }.isInstanceOf(SourceNotFoundException::class.java)
        }

    }

    @Nested
    inner class DeleteSource {

        @Test
        fun `should delete the source`() {
            sourceRepository.deleteSource(source1.id!!, source1.userId)

            assertThat(getSavedSource(source1.id!!)).isNull()
        }

        @Test
        fun `should fail when source does not exist`() {
            assertThatThrownBy { sourceRepository.deleteSource(999999, source1.userId) }
                .isInstanceOf(SourceNotFoundException::class.java)
        }

        @Test
        fun `should fail when source does not belong to the user`() {
            assertThatThrownBy { sourceRepository.deleteSource(source1.id!!, "2") }
                .isInstanceOf(SourceNotFoundException::class.java)
        }

    }

    private fun getSavedSource(id: Long): Source? {
        return jdbcTemplate.query("SELECT * FROM sources WHERE id = '$id'", SourceRowMapper()).firstOrNull()
    }

}
