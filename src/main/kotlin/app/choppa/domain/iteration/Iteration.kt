package app.choppa.domain.iteration

import app.choppa.domain.account.Account
import app.choppa.domain.account.Account.Companion.UNASSIGNED_ACCOUNT
import app.choppa.domain.base.BaseModel
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.hibernate.annotations.GenericGenerator
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit.DAYS
import java.util.*
import java.util.UUID.randomUUID
import javax.persistence.*
import kotlin.Int.Companion.MAX_VALUE

@Entity
@Table(name = "iteration")
@JsonSerialize(using = IterationSerializer::class)
@JsonDeserialize(using = IterationDeserializer::class)
data class Iteration(
    @Id
    @Column(name = "iteration_id", columnDefinition = "uuid")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    override val id: UUID = randomUUID(),

    @Column(name = "number", columnDefinition = "INTEGER", nullable = false)
    val number: Int = (1..MAX_VALUE).random(),

    @Column(name = "start_date", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    val startDate: Instant = now(),

    @Column(name = "end_date", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    val endDate: Instant = startDate.plus(14, DAYS),

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    override val account: Account = UNASSIGNED_ACCOUNT,
) : BaseModel {
    override fun toString() = "Iteration(id=$id, number=$number, startDate=$startDate, endDate=$endDate)"

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Iteration
        if (id != other.id) return false
        return true
    }
}
