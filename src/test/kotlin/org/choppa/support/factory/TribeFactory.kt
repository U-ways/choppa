package org.choppa.support.factory

import org.choppa.model.history.History
import org.choppa.model.tribe.Tribe

class TribeFactory {
    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        /**
         * Creates a random Tribe with X amount of squads and Y amount of members per squad.
         *
         * @param SquadAmount Int the number of squads each tribe has. (default = 0)
         * @param membersPerSquadAmount Int the number of members per squad. (default = 0)
         * @return Tribe
         */
        fun create(
            SquadAmount: Int = 0,
            membersPerSquadAmount: Int = 0
        ): Tribe = Tribe(squads = SquadFactory.create(SquadAmount, membersPerSquadAmount)).apply {
            this.squads.forEach { squad ->
                squad.members.forEach { member ->
                    member.history.add(
                        History(
                            iteration = IterationFactory.create(),
                            squad = squad,
                            member = member,
                            tribe = this
                        )
                    )
                }
            }
        }

        /**
         * Create X amount of tribes with Y amount of squads with Z amount of members in each squad.
         *
         * @param amount Int the number of tribes to create.
         * @param SquadAmount Int the number of squads each tribe has. (default = 0)
         * @param membersPerSquadAmount Int the number of members per squad. (default = 0)
         * @return MutableList<Tribe>
         */
        fun create(
            amount: Int,
            SquadAmount: Int = 0,
            membersPerSquadAmount: Int = 0
        ): MutableList<Tribe> = (0 until amount).map { this.create(SquadAmount, membersPerSquadAmount) }.toMutableList()
    }
}
