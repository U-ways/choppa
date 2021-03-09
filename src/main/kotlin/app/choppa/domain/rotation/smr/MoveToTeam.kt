package app.choppa.domain.rotation.smr
import app.choppa.domain.member.Member
import app.choppa.domain.squad.Squad

class MoveToTeam(
    val member: Member,
    val from: Squad,
    val to: Squad
)
