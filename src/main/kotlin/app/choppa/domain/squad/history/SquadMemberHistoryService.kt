package app.choppa.domain.squad.history

import app.choppa.domain.account.Account
import app.choppa.domain.member.Member
import app.choppa.domain.member.MemberRepository
import app.choppa.domain.squad.Squad
import app.choppa.domain.squad.history.RevisionType.ADD
import app.choppa.domain.squad.history.RevisionType.REMOVE
import app.choppa.exception.EntityNotFoundException
import org.hibernate.type.IntegerType.ZERO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest.of
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Pageable.unpaged
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.domain.Sort.by
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation.REPEATABLE_READ
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SquadMemberHistoryService(
    @Autowired private val squadHistoryRepository: SquadMemberHistoryRepository,
    @Autowired private val memberRepository: MemberRepository,
) {
    fun find(account: Account, pageable: Pageable = unpaged()): Page<SquadMemberHistory> = squadHistoryRepository
        .findAllByAccountIdOrderByCreateDateDesc(account.id, pageable)
        .orElseThrow { throw EntityNotFoundException("No Squad Member History records exist yet.") }

    fun findBySquad(
        squad: Squad,
        sort: Sort = by(DESC, SquadMemberHistory::createDate.name)
    ): List<SquadMemberHistory> = squadHistoryRepository
        .findAllBySquad(squad, sort)

    fun findByMember(
        member: Member,
        sort: Sort = by(DESC, SquadMemberHistory::createDate.name)
    ): List<SquadMemberHistory> = squadHistoryRepository
        .findAllByMember(member, sort)

    fun save(entity: SquadMemberHistory): SquadMemberHistory = squadHistoryRepository
        .save(entity)

    fun save(entities: List<SquadMemberHistory>): List<SquadMemberHistory> = squadHistoryRepository
        .saveAll(entities)

    fun deleteAllFor(member: Member) = member
        .apply { squadHistoryRepository.deleteAllByMember(member) }

    fun deleteAllFor(squad: Squad) = squad
        .apply { squadHistoryRepository.deleteAllBySquad(squad) }

    fun delete(entity: SquadMemberHistory): SquadMemberHistory = entity
        .apply { squadHistoryRepository.delete(entity) }

    fun delete(entities: List<SquadMemberHistory>): List<SquadMemberHistory> = entities
        .apply { squadHistoryRepository.deleteAll(entities) }

    @Transactional
    fun generateRevisions(squad: Squad, olderFormation: List<Member>) = with(nextRevisionNumber(squad)) {
        differentiate(squad.members, olderFormation).map { (member, revisionType) ->
            SquadMemberHistory(squad, member, this, revisionType)
        }
    }

    @Transactional
    fun concentrateAllSquadRevisions(squad: Squad): List<List<Member>> = squadHistoryRepository
        .findAllBySquad(squad)
        .foldRight(listOf()) { record: SquadMemberHistory, accumulator: List<List<Member>> ->
            when (accumulator.size) {
                0 ->
                    accumulator
                        .plusElement(listOf(memberRepository.findById(record.member.id).get()))
                else ->
                    accumulator
                        .dropLastIf(accumulator.size != record.revisionNumber)
                        .plusElement(
                            when (record.revisionType) {
                                ADD -> accumulator.last().plus(memberRepository.findById(record.member.id).get())
                                REMOVE -> accumulator.last().minus(record.member)
                            }
                        )
            }
        }

    @Transactional
    fun getSquadHistoriesAndDurations(squads: List<Squad>): List<Pair<Squad, List<Pair<Int, List<Member>>>>> {
        val squadHistoriesList: List<List<SquadMemberHistory>> = squads.map {
            squadHistoryRepository.findAllBySquad(it, by(Sort.Direction.ASC, SquadMemberHistory::createDate.name))
        }

        val squadsAndDurations = squadHistoriesList.map {
            it.foldRight(listOf()) { record: SquadMemberHistory, accumulator: List<Pair<Instant, List<Member>>> ->
                when (accumulator.size) {
                    0 ->
                        accumulator
                            .plusElement(
                                Pair(
                                    record.createDate,
                                    listOf(memberRepository.findById(record.member.id).get())
                                )
                            )
                    else ->
                        accumulator
                            .dropLastIf(accumulator.size != record.revisionNumber)
                            .plusElement(
                                Pair(
                                    record.createDate,
                                    when (record.revisionType) {
                                        ADD -> accumulator.last().second.plus(
                                            listOf(
                                                memberRepository.findById(record.member.id).get()
                                            )
                                        )
                                        REMOVE -> accumulator.last().second.minus(record.member)
                                    }
                                )
                            )
                }
            }
        }.map {
            var lastRecord = it[0]
            it.foldRight(listOf()) { current: Pair<Instant, List<Member>>, accumulator: List<Pair<Int, List<Member>>> ->
                if (lastRecord != current)
                    accumulator.plusElement(
                        Pair(
                            timestampDifferenceInDays(lastRecord.first, current.first),
                            lastRecord.second
                        )
                    )
                if (accumulator.size == it.size - 1)
                    accumulator.plusElement(
                        Pair(
                            timestampDifferenceInDays(current.first, Instant.now()),
                            current.second
                        )
                    )
                lastRecord = current
                accumulator
            }
        }
        return squads.mapIndexed { index, squad -> Pair(squad, squadsAndDurations[index]) }
    }

    private fun calculateMemberPairingPoints(squadsAndDurations: List<Pair<Int, List<Member>>>): HashMap<Pair<Member, Member>, Int> {
        val mppMap = HashMap<Pair<Member, Member>, Int>()

        squadsAndDurations.map {
            it.second
        }.flatten().distinct().apply {
            this.forEachIndexed { index, member ->
                if (index + 1 < this.size) {
                    this.subList(index + 1, this.size).forEach { subMember ->
                        mppMap[Pair(member, subMember)] = 0
                        mppMap[Pair(subMember, member)] = 0
                    }
                }
            }
        }

        squadsAndDurations.forEach {
            val time = it.first
            val members = it.second

            members.forEachIndexed { index, member ->
                if (index + 1 < members.size) {
                    members.subList(index + 1, members.size).forEach { subMember ->
                        mppMap[Pair(member, subMember)]?.plus(time)!!
                        mppMap[Pair(subMember, member)]?.plus(time)!!
                    }
                }
            }
        }
        return mppMap
    }

    private fun calculateSquadPairingPoints(squadConfigurationsAndDurations: List<Pair<Squad, List<Pair<Int, List<Member>>>>>): HashMap<Pair<Squad, Member>, Int> {
        val sppMap = HashMap<Pair<Squad, Member>, Int>()
        val squads = squadConfigurationsAndDurations.map { it.first }
        val members =
            squadConfigurationsAndDurations.asSequence().map { it.second }.flatten().map { it.second }.flatten().distinct()
                .toList()

        squads.forEach { squad ->
            members.forEach { member ->
                sppMap[Pair(squad, member)] = 0
            }
        }

        squadConfigurationsAndDurations.forEach { configurations ->
            val squad = configurations.first
            val configurationInstance = configurations.second
            configurationInstance.forEach { configuration ->
                val duration = configuration.first
                configuration.second.forEach { member ->
                    sppMap[Pair(squad, member)]?.plus(duration)
                }
            }
        }

        return sppMap
    }

    private fun timestampDifferenceInDays(start: Instant, end: Instant): Int {
        val millisToDays = 1000 * 60 * 60 * 24
        return (start.minusMillis(end.toEpochMilli()).toEpochMilli() / millisToDays).toInt()
    }

    fun getMPPAndSPP(squads: List<Squad>): Pair<HashMap<Pair<Member, Member>, Int>, HashMap<Pair<Squad, Member>, Int>> {
        val squadConfigurationsAndDurations = getSquadHistoriesAndDurations(squads)
        val sppMap = calculateSquadPairingPoints(squadConfigurationsAndDurations)
        val mppMap = calculateMemberPairingPoints(squadConfigurationsAndDurations.map { it.second }.flatten())
        return Pair(mppMap, sppMap)
    }

    @Transactional(isolation = REPEATABLE_READ)
    fun concentrateSquadRevisionsTo(squad: Squad, revisionNumber: Int): List<Member> = squadHistoryRepository
        .findAllBySquadAndRevisionNumberAfter(squad, revisionNumber)
        .backtrack(squad.members)

    @Transactional(isolation = REPEATABLE_READ)
    fun concentrateLastNSquadRevisions(squad: Squad, revisionAmount: Int): List<Member> = squadHistoryRepository
        .takeIf { revisionAmount > 0 }
        ?.findAllBySquad(squad, of(ZERO, revisionAmount))
        ?.backtrack(squad.members) ?: emptyList()

    private fun nextRevisionNumber(squad: Squad) = squadHistoryRepository
        .findAllBySquad(
            squad,
            of(ZERO, 1, by(DESC, SquadMemberHistory::revisionNumber.name))
        ).let {
            if (it.isEmpty()) ZERO else it.first().revisionNumber + 1
        }

    private fun differentiate(newerFormation: List<Member>, olderFormation: List<Member>) =
        with(newerFormation.intersect(olderFormation)) {
            newerFormation
                .minus(this)
                .map { Pair(it, ADD) }
                .plus(
                    olderFormation
                        .minus(this)
                        .map { Pair(it, REMOVE) }
                )
        }

    private fun List<SquadMemberHistory>.backtrack(members: List<Member>) = this
        .fold(members) { acc, record ->
            when (record.revisionType) {
                ADD -> acc.minus(record.member)
                REMOVE -> acc.plus(memberRepository.findById(record.member.id).get())
            }
        }.toMutableList()

    private fun <T> List<T>.dropLastIf(predicate: Boolean): List<T> =
        if (predicate) this.dropLast(1) else this

    private fun <T> Page<T>.orElseThrow(exception: () -> Nothing): Page<T> = when {
        this.isEmpty -> exception.invoke()
        else -> this
    }
}
