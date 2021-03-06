package app.choppa.integration.domain.chapter

import app.choppa.domain.account.Account.Companion.UNASSIGNED_ACCOUNT
import app.choppa.domain.account.AccountService
import app.choppa.domain.chapter.Chapter
import app.choppa.domain.chapter.ChapterController
import app.choppa.domain.chapter.ChapterService
import app.choppa.exception.EmptyListException
import app.choppa.support.factory.ChapterFactory
import app.choppa.utils.Color.Companion.toRGBAInt
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders.LOCATION
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

@WebMvcTest(controllers = [ChapterController::class])
@ActiveProfiles("test")
internal class ChapterControllerCollectionIT @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
) {
    @MockkBean
    private lateinit var chapterService: ChapterService

    @MockkBean(relaxed = true)
    private lateinit var accountService: AccountService

    @Nested
    inner class HappyPath {

        @Test
        fun `LIST entities`() {
            val entities = ChapterFactory.create(amount = 2)

            every { chapterService.find(UNASSIGNED_ACCOUNT) } returns entities

            mvc.get("/api/chapters") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isOk }
                content { contentType(APPLICATION_JSON) }
                content { json(mapper.writeValueAsString(entities)) }
            }
        }

        @Test
        fun `PUT collection`() {
            val existingCollection = ChapterFactory.create(amount = 3)
            val greenColor = "#00FF00".toRGBAInt()
            val updatedCollection = existingCollection.map { Chapter(it.id, it.name, greenColor) }

            every { chapterService.find(existingCollection.map { it.id }, UNASSIGNED_ACCOUNT) } returns existingCollection
            every { chapterService.save(updatedCollection, UNASSIGNED_ACCOUNT) } returns updatedCollection

            mvc.put("/api/chapters") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(updatedCollection)
            }.andExpect {
                status { isCreated }
                header { string(LOCATION, StringContains("/api/chapters")) }
            }
        }

        @Test
        fun `DELETE collection`() {
            val existingCollection = ChapterFactory.create(amount = 3)

            every { chapterService.find(existingCollection.map { it.id }, UNASSIGNED_ACCOUNT) } returns existingCollection
            every { chapterService.delete(existingCollection, UNASSIGNED_ACCOUNT) } returns existingCollection

            mvc.delete("/api/chapters") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(existingCollection)
            }.andExpect {
                status { isNoContent }
            }
        }

        @Test
        fun `POST collection`() {
            val newCollection = ChapterFactory.create(amount = 3)

            every { chapterService.save(newCollection, UNASSIGNED_ACCOUNT) } returns newCollection

            mvc.post("/api/chapters") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(newCollection)
            }.andExpect {
                status { isCreated }
                header { string(LOCATION, StringContains("/api/chapters")) }
            }
        }
    }

    @Nested
    inner class SadPath {

        @Test
        fun `LIST no content`() {
            every { chapterService.find(UNASSIGNED_ACCOUNT) } throws EmptyListException("No chapters exist yet")

            mvc.get("/api/chapters") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNoContent }
            }
        }
    }
}
