package app.choppa.acceptance.domain.tribe

import app.choppa.domain.account.Account.Companion.UNASSIGNED_ACCOUNT
import app.choppa.domain.squad.SquadService
import app.choppa.domain.tribe.Tribe
import app.choppa.domain.tribe.TribeRepository
import app.choppa.domain.tribe.TribeService
import app.choppa.exception.EntityNotFoundException
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional.empty
import java.util.Optional.of
import java.util.UUID.randomUUID

internal class TribeServiceTest {
    private lateinit var repository: TribeRepository
    private lateinit var squadService: SquadService
    private lateinit var service: TribeService

    @BeforeEach
    internal fun setUp() {
        repository = mockkClass(TribeRepository::class)
        squadService = mockkClass(SquadService::class)
        service = TribeService(repository, squadService)
    }

    @Test
    fun `Given new entity, when service saves new entity, then service should save in repository and return the same entity`() {
        val entity = Tribe()

        every { repository.findById(entity.id) } returns empty()
        every { repository.save(entity) } returns entity

        val savedEntity = service.save(entity, UNASSIGNED_ACCOUNT)

        savedEntity shouldBe entity

        verify(exactly = 1) { repository.save(entity) }
    }

    @Test
    fun `Given existing entity, when service looks for existing entity by id, then service should find using repository and return existing entity`() {
        val id = randomUUID()
        val existingEntity = Tribe(id)

        every { repository.findById(id) } returns of(existingEntity)

        val foundEntity = service.find(id, UNASSIGNED_ACCOUNT)

        foundEntity shouldBe existingEntity

        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `Given existing entity, when service deletes existing entity, then service should delete using repository`() {
        val existingEntity = Tribe()

        every { repository.findById(existingEntity.id) } returns of(existingEntity)
        every { repository.delete(existingEntity) } returns Unit
        every { squadService.deleteRelatedByTribe(existingEntity.id, UNASSIGNED_ACCOUNT) } returns existingEntity.squads

        val removedEntity = service.delete(existingEntity, UNASSIGNED_ACCOUNT)

        removedEntity shouldBe existingEntity

        verify(exactly = 1) { repository.delete(existingEntity) }
    }

    @Test
    fun `Given a non-existent entity UUID, when service tries to find by said UUID, then service should throw EntityNotFoundException`() {
        val id = randomUUID()

        every { repository.findById(id) } returns empty()

        assertThrows(EntityNotFoundException::class.java) { service.find(id, UNASSIGNED_ACCOUNT) }
    }
}
