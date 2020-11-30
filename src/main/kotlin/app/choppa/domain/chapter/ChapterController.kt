package app.choppa.domain.chapter

import app.choppa.domain.base.BaseController
import app.choppa.domain.base.BaseController.Companion.API_PREFIX
import app.choppa.domain.squad.Squad
import app.choppa.domain.tribe.Tribe
import app.choppa.utils.QueryComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("$API_PREFIX/chapters")
class ChapterController(
    @Autowired private val chapterService: ChapterService
) : BaseController<Chapter>(chapterService) {
    @GetMapping
    fun listChapters(
        @QueryComponent(Squad::class) @RequestParam(name = "squad", required = false) squadId: UUID?,
        @QueryComponent(Tribe::class) @RequestParam(name = "tribe", required = false) tribeId: UUID?,
    ): ResponseEntity<List<Chapter>> = ok().body(
        when {
            squadId is UUID -> chapterService.findRelatedBySquad(squadId)
            tribeId is UUID -> chapterService.findRelatedByTribe(tribeId)
            else -> chapterService.find()
        }
    )
}