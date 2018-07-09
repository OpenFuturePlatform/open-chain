package io.openfuture.chain.entity

import io.openfuture.chain.domain.delegate.AccountDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "accounts")
class Account(

        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "address", nullable = false)
        val address: String,

        @Column(name = "public_key", nullable = false, unique = true)
        val publicKey: String,

        @Column(name = "is_delegate", nullable = false)
        val isDelegate: Boolean = false,

        @Column(name = "rating", nullable = false)
        var rating: Int = 0,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "accounts_2_delegates",
                joinColumns = [(JoinColumn(name = "account_id", nullable = false))],
                inverseJoinColumns = [(JoinColumn(name = "delegate_id", nullable = false))]
        )
        val votes: MutableSet<Account> = mutableSetOf()

) : BaseModel() {

    companion object {
        fun of(delegateDto: AccountDto): Account = Account(
                delegateDto.username,
                delegateDto.address,
                delegateDto.publicKey
        )
    }

}