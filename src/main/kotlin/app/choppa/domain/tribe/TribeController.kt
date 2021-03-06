package app.choppa.domain.tribe

import app.choppa.domain.account.Account
import app.choppa.domain.base.BaseController
import app.choppa.domain.base.BaseController.Companion.API_PREFIX
import app.choppa.domain.rotation.RotationOptions
import app.choppa.domain.rotation.RotationOptions.Companion.DEFAULT_OPTIONS
import app.choppa.domain.rotation.RotationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import java.io.Serializable
import java.util.*

@RestController
@RequestMapping("$API_PREFIX/tribes")
class TribeController(
    @Autowired private val tribeService: TribeService,
    @Autowired private val rotationService: RotationService
) : BaseController<Tribe>(tribeService) {

    @GetMapping
    fun listTribes(account: Account): ResponseEntity<List<Tribe>> =
        ok().body(tribeService.find(account))

    @PutMapping
    fun putCollection(
        @RequestBody updatedCollection: List<Tribe>,
        account: Account,
    ): ResponseEntity<List<Tribe>> = tribeService
        .find(updatedCollection.map { it.id }, account)
        .also { tribeService.save(updatedCollection, account) }
        .run { created(location()).build() }

    @DeleteMapping
    fun deleteCollection(
        @RequestBody toDeleteCollection: List<Tribe>,
        account: Account,
    ): ResponseEntity<List<Tribe>> = tribeService
        .find(toDeleteCollection.map { it.id }, account)
        .also { tribeService.delete(toDeleteCollection, account) }
        .run { noContent().build() }

    @PostMapping
    fun postCollection(
        @RequestBody newCollection: List<Tribe>,
        account: Account,
    ): ResponseEntity<List<Tribe>> = tribeService
        .save(newCollection, account)
        .run { created(location()).build() }

    @PostMapping("$ID_PATH:rotate")
    fun executeRotation(
        @PathVariable id: UUID,
        @RequestBody(required = false) options: RotationOptions?,
        account: Account,
    ): ResponseEntity<Tribe> =
        ok().body(
            rotationService.executeRotation(
                tribeService.find(id, account),
                options ?: DEFAULT_OPTIONS,
                account
            )
        )

    @GetMapping("$ID_PATH:undoRotation")
    fun undoRotation(
        @PathVariable id: UUID,
        account: Account,
    ): ResponseEntity<Tribe> =
        ok().body(
            rotationService.undoRotation(tribeService.find(id, account), account)
        )

    @GetMapping("stats")
    fun getStatistics(account: Account): ResponseEntity<Map<String, Serializable>> = ok().body(tribeService.statistics(account))
}
