package app.choppa.integration.domain.tribe

import app.choppa.domain.account.Account
import app.choppa.domain.account.AccountService
import app.choppa.domain.rotation.RotationOptions
import app.choppa.domain.rotation.RotationService
import app.choppa.domain.tribe.Tribe
import app.choppa.domain.tribe.TribeController
import app.choppa.domain.tribe.TribeService
import app.choppa.exception.EmptyListException
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

@WebMvcTest(controllers = [TribeController::class])
@ActiveProfiles("test")
internal class TribeControllerIT @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper,
) {
    @MockkBean(relaxed = true)
    private lateinit var accountService: AccountService
    @MockkBean
    private lateinit var tribeService: TribeService
    @MockkBean
    private lateinit var rotationService: RotationService

    private lateinit var tribe: Tribe
    private lateinit var account: Account

    @BeforeEach
    internal fun setUp() {
        tribe = Tribe()
        account = Account()
    }

    @Nested
    inner class HappyPath {

        @Test
        fun `LIST entities`() {
            val anotherTribe = Tribe()
            val entities = listOf(tribe, anotherTribe)

            every { tribeService.find(account) } returns entities

            mvc.get("/api/tribes") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isOk }
                content { contentType(APPLICATION_JSON) }
                content { json(mapper.writeValueAsString(entities)) }
            }
        }

        @Test
        fun `GET entity by ID`() {
            val entity = tribe

            every { tribeService.find(entity.id, account) } returns entity

            mvc.get("/api/tribes/{id}", entity.id) {
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
            val entity = tribe
            val updatedEntity = Tribe(tribe.id, tribe.name)

            every { tribeService.find(entity.id, account) } returns entity
            every {
                tribeService.save(
                    Tribe(
                        tribe.id,
                        tribe.name
                    ),
                    account
                )
            } returns updatedEntity

            mvc.put("/api/tribes/{id}", entity.id) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(updatedEntity)
            }.andExpect {
                status { isCreated }
                header { string(LOCATION, StringContains("/api/tribes/${entity.id}")) }
            }
        }

        @Test
        fun `DELETE entity by ID`() {
            val entity = tribe

            every { tribeService.find(entity.id, account) } returns entity
            every { tribeService.delete(entity, account) } returns entity

            mvc.delete("/api/tribes/{id}", entity.id) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNoContent }
            }
        }

        @Test
        fun `POST new entity`() {
            val newEntity = tribe

            every { tribeService.save(Tribe(tribe.id, tribe.name), account) } returns newEntity

            mvc.post("/api/tribes/${newEntity.id}") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(newEntity)
            }.andExpect {
                status { isCreated }
                header { string(LOCATION, StringContains("/api/tribes/${newEntity.id}")) }
            }
        }

        @Test
        fun `POST rotation request no payload`() {
            val entity = tribe

            every { rotationService.executeRotation(tribe, RotationOptions.DEFAULT_OPTIONS, account) } returns entity
            every { tribeService.find(entity.id, account) } returns entity

            mvc.post("/api/tribes/${entity.id}:rotate") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isOk }
                content { contentType(APPLICATION_JSON) }
                content { json(mapper.writeValueAsString(entity)) }
            }
        }

        @Test
        fun `POST rotation request with valid payload`() {
            val entity = tribe
            val rotation = RotationOptions.DEFAULT_OPTIONS

            every { rotationService.executeRotation(tribe, RotationOptions.DEFAULT_OPTIONS, account) } returns entity
            every { tribeService.find(entity.id, account) } returns entity

            mvc.post("/api/tribes/${entity.id}:rotate") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = mapper.writeValueAsString(rotation)
            }.andExpect {
                status { isOk }
                content { contentType(APPLICATION_JSON) }
                content { json(mapper.writeValueAsString(entity)) }
            }
        }
    }

    @Nested
    inner class SadPath {

        @Test
        fun `LIST no content`() {
            every { tribeService.find(account) } throws EmptyListException("No tribes exist yet")

            mvc.get("/api/tribes") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNoContent }
            }
        }

        @Test
        fun `GET UUID doesn't exist`() {
            val randomUUID = randomUUID()

            every { tribeService.find(randomUUID, account) } throws EntityNotFoundException("Tribe with id [$randomUUID] does not exist.")

            mvc.get("/api/tribes/{id}", randomUUID) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNotFound }
            }
        }

        @Test
        fun `PUT invalid payload`() {
            val randomUUID = randomUUID()

            mvc.put("/api/tribes/{id}", randomUUID) {
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

            every { tribeService.find(randomUUID, account) } throws EntityNotFoundException("Tribe with id [$randomUUID] does not exist.")

            mvc.delete("/api/tribes/{id}", randomUUID) {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNotFound }
            }
        }

        @Test
        fun `POST invalid payload`() {
            mvc.post("/api/tribes") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = "invalidPayload"
            }.andExpect {
                status { isUnprocessableEntity }
            }
        }

        @Test
        fun `POST rotation request with invalid payload`() {
            val entity = tribe

            mvc.post("/api/tribes/${entity.id}:rotate") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
                content = "invalidPayload"
            }.andExpect {
                status { isUnprocessableEntity }
            }
        }

        @Test
        fun `POST rotation request to invalid tribe`() {
            val randomUUID = randomUUID()

            every { tribeService.find(randomUUID, account) } throws EntityNotFoundException("Tribe with id [$randomUUID] does not exist.")

            mvc.post("/api/tribes/$randomUUID:rotate") {
                contentType = APPLICATION_JSON
                accept = APPLICATION_JSON
            }.andExpect {
                status { isNotFound }
            }
        }
    }
}
