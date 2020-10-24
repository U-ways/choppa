package org.choppa.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.GenericGenerator
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "chapter")
data class Chapter @JsonCreator constructor(
    @Id
    @Column(name = "chapter_id", columnDefinition = "uuid")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonProperty("id")
    val id: UUID,

    @Column(name = "name", columnDefinition = "VARCHAR(100)", unique = true, nullable = false)
    @JsonProperty("name")
    val name: String,

    @OneToMany(mappedBy = "chapter")
    @JsonIgnore
    val member: List<Member>
) {
    override fun toString() = "Chapter(id=$id, name=$name)"
}
