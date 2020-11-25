package org.choppa.acceptance.domain.member

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.choppa.domain.chapter.Chapter
import org.choppa.domain.history.History
import org.choppa.domain.iteration.Iteration
import org.choppa.domain.member.Member
import org.choppa.domain.member.Member.Companion.NO_MEMBERS
import org.choppa.domain.squad.Squad
import org.choppa.domain.tribe.Tribe
import org.junit.jupiter.api.Test

class MemberTest {
    @Test
    internal fun `NO_MEMBERS static should not pass by reference`() {
        val member = Member()
        val newNoMembersMutableList = NO_MEMBERS

        newNoMembersMutableList.add(member)

        assert(NO_MEMBERS.isEmpty())
    }

    @Test
    fun `verify the equals and hashCode contract`() {
        EqualsVerifier
            .configure().suppress(Warning.SURROGATE_KEY)
            .forClass(Member::class.java)
            .withPrefabValues(Chapter::class.java, Chapter(), Chapter())
            .withPrefabValues(Squad::class.java, Squad(), Squad())
            .withPrefabValues(History::class.java, History(Iteration(), Tribe(), Squad(), Member()), History(Iteration(), Tribe(), Squad(), Member()))
            .verify()
    }
}
