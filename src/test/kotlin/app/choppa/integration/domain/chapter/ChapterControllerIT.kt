package app.choppa.integration.domain.chapter

import app.choppa.domain.account.Account.Companion.UNASSIGNED_ACCOUNT
import app.choppa.domain.chapter.Chapter
import app.choppa.domain.chapter.ChapterController
import app.choppa.domain.chapter.ChapterService
import app.choppa.exception.EntityNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders.LOCATION
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import java.util.UUID.randomUUID

@WebMvcTest(controllers = [ChapterController::class])
@ActiveProfiles("test")
internal class ChapterControllerIT @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
) {
    @MockkBean
    private lateinit var chapterService: ChapterService
    private lateinit var chapter: Chapter

    @BeforeEach
    internal fun setUp() {
        chapter = Chapter()
    }

    @Nested
    inner class HappyPath {
        @Test
        fun `GET entity by ID`() {
            val entity = chapter

            every { chapterService.find(entity.id, UNASSIGNED_ACCOUNT) } returns entity

            mvc.get("/api/chapters/{id}", entity.id) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isOk }
                content { contentType(APPLICATION_JSON) }
                content { json(mapper.writeValueAsString(entity)) }
            }
        }

        @Test
        fun `PUT entity by ID`() {
            val entity = chapter
            val updatedEntity = Chapter(chapter.id)

            every { chapterService.find(entity.id, UNASSIGNED_ACCOUNT) } returns entity
            every { chapterService.save(updatedEntity, UNASSIGNED_ACCOUNT) } returns updatedEntity

            mvc.put("/api/chapters/{id}", entity.id) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(updatedEntity)
            }.andExpect {
                status { isCreated }
                header { string(LOCATION, StringContains("/api/chapters/${entity.id}")) }
            }
        }

        @Test
        fun `DELETE entity by ID`() {
            val entity = chapter

            every { chapterService.find(entity.id, UNASSIGNED_ACCOUNT) } returns entity
            every { chapterService.delete(entity, UNASSIGNED_ACCOUNT) } returns entity

            mvc.delete("/api/chapters/{id}", entity.id) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNoContent }
            }
        }

        @Test
        fun `POST new entity`() {
            val newEntity = chapter

            every { chapterService.save(newEntity, UNASSIGNED_ACCOUNT) } returns newEntity

            mvc.post("/api/chapters/${newEntity.id}") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(newEntity)
            }.andExpect {
                status { isCreated }
                header { string(LOCATION, StringContains("/api/chapters/${newEntity.id}")) }
            }
        }
    }

    @Nested
    inner class SadPath {
        @Test
        fun `GET UUID doesn't exist`() {
            val randomUUID = randomUUID()

            every { chapterService.find(randomUUID, UNASSIGNED_ACCOUNT) } throws EntityNotFoundException("Chapter with id [$randomUUID] does not exist.")

            mvc.get("/api/chapters/{id}", randomUUID) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNotFound }
            }
        }

        @Test
        fun `PUT invalid payload`() {
            val randomUUID = randomUUID()

            mvc.put("/api/chapters/{id}", randomUUID) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = "invalidPayload"
            }.andExpect {
                status { isUnprocessableEntity }
            }
        }

        @Test
        fun `DELETE UUID doesn't exist`() {
            val randomUUID = randomUUID()

            every { chapterService.find(randomUUID, UNASSIGNED_ACCOUNT) } throws EntityNotFoundException("Chapter with id [$randomUUID] does not exist.")

            mvc.delete("/api/chapters/{id}", randomUUID) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNotFound }
            }
        }

        @Test
        fun `POST invalid payload`() {
            mvc.post("/api/chapters/${chapter.id}") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = "invalidPayload"
            }.andExpect {
                status { isUnprocessableEntity }
            }
        }

        @Test
        fun `POST invalid color payload`() {
            mvc.post("/api/chapters/${chapter.id}") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content =
                    """
                        {
                            "id": "squads/${chapter.id}",
                            "name": "${chapter.name}",
                            "color": "#123456789"
                        }
                    """.trimIndent()
            }.andExpect {
                status { isUnprocessableEntity }
            }
        }
    }
}
