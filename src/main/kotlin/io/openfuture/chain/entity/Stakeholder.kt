package io.openfuture.chain.entity

import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.peer.Delegate
import javax.persistence.*

@Entity
@Table(name = "stakeholders")
class Stakeholder(

    @Column(name = "address", nullable = false)
    val address: String,

    @Column(name = "public_key", nullable = false, unique = true)
    val publicKey: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "stakeholders_2_delegates",
        joinColumns = [(JoinColumn(name = "stakeholder_id", nullable = false))],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id", nullable = false))]
    )
    val votes: MutableSet<Delegate> = mutableSetOf()

) : BaseModel() {

    companion object {
        fun of(dto: StakeholderDto): Stakeholder = Stakeholder(
            dto.address,
            dto.publicKey
        )
    }

}