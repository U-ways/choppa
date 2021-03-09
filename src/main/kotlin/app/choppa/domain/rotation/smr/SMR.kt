package app.choppa.domain.rotation.smr

import app.choppa.domain.chapter.Chapter
import app.choppa.domain.member.Member
import app.choppa.domain.squad.Squad
import app.choppa.domain.squad.history.SquadMemberHistoryService
import org.springframework.beans.factory.annotation.Autowired

class SMR(
    @Autowired private val squadHistoryService: SquadMemberHistoryService
) {
    companion object {
        var newMPPScalar = 0.1f
        var newSPPScalar = 1.0f
    }

    fun SelectRotations(squads: List<Squad>, amount: Int, chapter: Chapter): List<MoveToTeam> {
        if (amount < 1) return listOf()

        val currentMemberSquads = squads.map { Pair(it, it.members.toList().filter { x -> x.chapter != chapter && x.squads.size > 1 }) }
            .filter { it.second.count() > 0 }

        val tempMemberSquads = currentMemberSquads.map { Pair(it.first, it.second.toMutableList()) }.toMutableList()
        val activeMemberList = currentMemberSquads.map { it.second }.flatten().toMutableList()
        val mppAndSpp = squadHistoryService.getMPPAndSPP(currentMemberSquads.map { it.first })
        val mpp = mppAndSpp.first
        val spp = mppAndSpp.second

        val proposedMoves = mutableListOf<MoveToTeam>()

        fun averageMpp(x: Member): Double = activeMemberList.filter { y -> y != x }.map { y -> mpp[Pair(x, y)]!! }.average()

        fun averageMppByTeam(x: Member, teams: List<Pair<Squad, List<Member>>>, averageMpp: Double): List<Pair<Squad, Double>> =
            teams.map { Pair(it.first, it.second.filter { m -> m != x }.map { y -> mpp[Pair(x, y)]!! - averageMpp }.average()) }

        fun memberSppAndMppByTeam(x: Member, averageMppByTeam: List<Pair<Squad, Double>>): List<Triple<Squad, Int, Double>> =
            averageMppByTeam.map { y -> Triple(y.first, spp[Pair(x.squads[0], x)]!! - spp[Pair(y.first, x)]!!, y.second) }

        fun optimalMove(conditions: List<Triple<Squad, Int, Double>>): Pair<Squad, Double> =
            conditions.map { x -> Pair(x.first, (x.second * newSPPScalar) + (-x.third * newMPPScalar)) }.maxByOrNull { it.second }!!

        fun handleMove(move: Triple<Member, Pair<Squad, List<Member>>, Double>) {
            proposedMoves.add(MoveToTeam(move.first, move.first.squads[0], move.second.first))
            activeMemberList.remove(move.first)
            tempMemberSquads.find { it.first == move.first.squads[0] }!!.second.remove(move.first)
        }

        (0 until amount).forEach { _ ->
            val ksps: List<Triple<Member, Pair<Squad, List<Member>>, Double>> = activeMemberList
                .map { x ->
                    val memberSppAndMppByTeam = memberSppAndMppByTeam(x, averageMppByTeam(x, tempMemberSquads, averageMpp(x)))
                    val optimalMove: Pair<Squad, Double> = optimalMove(memberSppAndMppByTeam)
                    Triple(x, tempMemberSquads.find { it.first == optimalMove.first }!!, optimalMove.second)
                }.filter { x -> x.second.first != x.first.squads[0] }

            if (ksps.isEmpty()) return proposedMoves
            val best = ksps.maxByOrNull { x -> x.third }!!
            handleMove(best)

            val secondaryKSPs = best.second.second.filter { activeMemberList.contains(it) }.map { x ->
                val memberSppAndMppByTeam = memberSppAndMppByTeam(x, averageMppByTeam(x, listOf(best.second), averageMpp(x)))
                val optimalMove: Pair<Squad, Double> = optimalMove(memberSppAndMppByTeam)
                Triple(x, tempMemberSquads.find { it.first == best.first.squads[0] }!!, optimalMove.second)
            }

            if (secondaryKSPs.isEmpty()) return proposedMoves
            val bestSecondary = secondaryKSPs.maxByOrNull { x -> x.third }!!
            handleMove(bestSecondary)

            // For some reason best is returning a list instance instead of a mutable list, I do not know why.
            tempMemberSquads.find { it.first == best.first.squads[0] }!!.second.add(bestSecondary.first)
            tempMemberSquads.find { it.first == bestSecondary.first.squads[0] }!!.second.add(best.first)

            tempMemberSquads.removeIf { it.second.none { x -> activeMemberList.contains(x) } }
        }

        return proposedMoves
    }
}
