package io.openfuture.chain.entity.account

import io.openfuture.chain.domain.delegate.AccountDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.JOINED)
open class Account(

        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "address", nullable = false)
        val address: String,

        @Column(name = "public_key", nullable = false, unique = true)
        val publicKey: String,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "accounts_2_delegates",
                joinColumns = [(JoinColumn(name = "account_id", nullable = false))],
                inverseJoinColumns = [(JoinColumn(name = "delegate_id", nullable = false))]
        )
        val votes: MutableSet<Delegate> = mutableSetOf()

) : BaseModel() {

    companion object {
        fun of(delegateDto: AccountDto): Account = Account(
                delegateDto.username,
                delegateDto.address,
                delegateDto.publicKey
        )
    }

}