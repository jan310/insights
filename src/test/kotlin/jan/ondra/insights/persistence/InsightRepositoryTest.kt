package jan.ondra.insights.persistence

import jan.ondra.insights.exception.InsightNotFoundException
import jan.ondra.insights.exception.SourceDoesNotBelongToUserException
import jan.ondra.insights.exception.SourceNotFoundException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.FilterTag.PERSONAL_DEVELOPMENT
import jan.ondra.insights.models.FilterTag.WEALTH_CREATION
import jan.ondra.insights.models.Insight
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
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("test")
@JdbcTest
@Import(InsightRepository::class)
@AutoConfigureTestDatabase(replace = NONE)
class InsightRepositoryTest(
    @Autowired private val insightRepository: InsightRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate
) {

    private lateinit var source1: Source
    private lateinit var source2: Source
    private lateinit var source3: Source

    private lateinit var insight1: Insight
    private lateinit var insight2: Insight
    private lateinit var insight3: Insight
    private lateinit var insight4: Insight

    @BeforeEach
    fun setup() {
        jdbcTemplate.update(
            """
                DELETE FROM users;
                
                INSERT INTO users
                    (id, email, notification_enabled, notification_filter_tags)
                VALUES
                    ('1', 'user1@email.com', true, ARRAY[]::VARCHAR[]),
                    ('2', 'user2@email.com', true, ARRAY[]::VARCHAR[]);
                    
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
        source3 = sources[2]

        jdbcTemplate.update(
            """
                INSERT INTO insights
                    (user_id, source_id, last_modified_date, filter_tags, note, quote)
                VALUES
                    (1, '${source1.id}', '2024-12-04', ARRAY[]::VARCHAR[], 'note1', 'quote1'),
                    (1, '${source2.id}', '2024-12-03', ARRAY[]::VARCHAR[], 'note2', 'quote2'),
                    (1, null, '2024-12-02', ARRAY[]::VARCHAR[], 'note3', 'quote3'),
                    (2, '${source3.id}', '2024-12-01', ARRAY[]::VARCHAR[], 'note4', 'quote4'),
                    (2, null, '2024-12-01', ARRAY[]::VARCHAR[], 'note4', 'quote4');
            """.trimIndent()
        )

        val insights = jdbcTemplate.query("SELECT * FROM insights", InsightRowMapper()).apply { sortBy { it.id } }
        insight1 = insights[0]
        insight2 = insights[1]
        insight3 = insights[2]
        insight4 = insights[3]
    }

    @Nested
    inner class CreateInsight {

        @Test
        fun `should save the insight`() {
            val insight = Insight(
                userId = "1",
                sourceId = source1.id,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(WEALTH_CREATION),
                note = "The money is always right!",
                quote = null
            )

            val insightId = insightRepository.createInsight(insight)

            assertThat(getSavedInsight(insightId)).isEqualTo(insight.copy(id = insightId))
        }

        @Test
        fun `should save the insight when no source is provided`() {
            val insight = Insight(
                userId = "1",
                sourceId = null,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(),
                note = "The money is always right!",
                quote = null
            )

            val insightId = insightRepository.createInsight(insight)

            assertThat(getSavedInsight(insightId)).isEqualTo(insight.copy(id = insightId))
        }

        @Test
        fun `should fail when the user does not exist`() {
            val insight = Insight(
                userId = "999999",
                sourceId = null,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(),
                note = "The money is always right!",
                quote = null
            )

            assertThatThrownBy { insightRepository.createInsight(insight) }
                .isInstanceOf(UserNotRegisteredException::class.java)
                .hasCauseInstanceOf(DataIntegrityViolationException::class.java)
                .cause().message().contains(
                    "insert or update on table \"insights\" violates foreign key constraint \"insights_user_id_fkey\""
                )
        }

        @Test
        fun `should fail when the source does not exist`() {
            val insight = Insight(
                userId = "1",
                sourceId = 999999,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(),
                note = "The money is always right!",
                quote = null
            )

            assertThatThrownBy { insightRepository.createInsight(insight) }
                .isInstanceOf(SourceNotFoundException::class.java)
                .hasCauseInstanceOf(EmptyResultDataAccessException::class.java)
                .cause().message().contains("Incorrect result size: expected 1, actual 0")
        }

        @Test
        fun `should fail when the source belongs to another user`() {
            val insight = Insight(
                userId = "2",
                sourceId = source1.id,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(),
                note = "The money is always right!",
                quote = null
            )

            assertThatThrownBy { insightRepository.createInsight(insight) }
                .isInstanceOf(SourceDoesNotBelongToUserException::class.java)
        }

    }

    @Nested
    inner class GetInsights {

        @Test
        fun `should get all insights from the user` () {
            assertThat(insightRepository.getInsights("1")).containsExactlyInAnyOrder(insight1, insight2, insight3)
        }

    }

    @Nested
    inner class UpdateInsight {

        @Test
        fun `should update the insight`() {
            val updatedInsight = Insight(
                id = insight1.id,
                userId = "1",
                sourceId = source2.id,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                note = "The money is always right!",
                quote = null
            )

            insightRepository.updateInsight(updatedInsight)

            assertThat(getSavedInsight(insight1.id!!)).isEqualTo(updatedInsight)
        }

        @Test
        fun `should update the insight when no source is provided`() {
            val updatedInsight = Insight(
                id = insight1.id,
                userId = "1",
                sourceId = null,
                lastModifiedDate = LocalDate.now(),
                filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                note = "The money is always right!",
                quote = null
            )

            insightRepository.updateInsight(updatedInsight)

            assertThat(getSavedInsight(insight1.id!!)).isEqualTo(updatedInsight)
        }

        @Test
        fun `should fail when the insight does not exist`() {
            assertThatThrownBy {
                insightRepository.updateInsight(
                    Insight(
                        id = 999999,
                        userId = "1",
                        sourceId = null,
                        lastModifiedDate = LocalDate.now(),
                        filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                        note = "The money is always right!",
                        quote = null
                    )
                )
            }.isInstanceOf(InsightNotFoundException::class.java)
        }

        @Test
        fun `should fail when the insight does not belong to the user`() {
            assertThatThrownBy {
                insightRepository.updateInsight(
                    Insight(
                        id = insight4.id,
                        userId = "1",
                        sourceId = null,
                        lastModifiedDate = LocalDate.now(),
                        filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                        note = "The money is always right!",
                        quote = null
                    )
                )
            }.isInstanceOf(InsightNotFoundException::class.java)
        }

        @Test
        fun `should fail when the source does not exist`() {
            assertThatThrownBy {
                insightRepository.updateInsight(
                    Insight(
                        id = insight1.id,
                        userId = "1",
                        sourceId = 999999,
                        lastModifiedDate = LocalDate.now(),
                        filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                        note = "The money is always right!",
                        quote = null
                    )
                )
            }
                .isInstanceOf(SourceNotFoundException::class.java)
                .hasCauseInstanceOf(EmptyResultDataAccessException::class.java)
                .cause().message().contains("Incorrect result size: expected 1, actual 0")
        }

        @Test
        fun `should fail when the source does not belong to the user`() {
            assertThatThrownBy {
                insightRepository.updateInsight(
                    Insight(
                        id = insight1.id,
                        userId = "1",
                        sourceId = source3.id,
                        lastModifiedDate = LocalDate.now(),
                        filterTags = listOf(WEALTH_CREATION, PERSONAL_DEVELOPMENT),
                        note = "The money is always right!",
                        quote = null
                    )
                )
            }.isInstanceOf(SourceDoesNotBelongToUserException::class.java)
        }

    }

    @Nested
    inner class DeleteInsight {

        @Test
        fun `should delete the insight`() {
            insightRepository.deleteInsight(insight1.id!!, "1")

            assertThat(getSavedInsight(insight1.id!!)).isNull()
        }

        @Test
        fun `should fail when the insight does not exist`() {
            assertThatThrownBy { insightRepository.deleteInsight(999999, "1") }
                .isInstanceOf(InsightNotFoundException::class.java)
        }

        @Test
        fun `should fail when the insight does not belong to the user`() {
            assertThatThrownBy { insightRepository.deleteInsight(insight1.id!!, "2") }
                .isInstanceOf(InsightNotFoundException::class.java)
        }

    }

    private fun getSavedInsight(id: Long): Insight? {
        return jdbcTemplate.query("SELECT * FROM insights WHERE id = $id", InsightRowMapper()).firstOrNull()
    }

}
