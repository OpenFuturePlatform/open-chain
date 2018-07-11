package io.openfuture.chain.repository

import io.openfuture.chain.entity.*
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.entity.account.Delegate
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<Block> {

    fun findFirstByOrderByHeightDesc(): Block?

}

@Repository
interface GenesisBlockRepository : BaseRepository<GenesisBlock>

@Repository
interface MainBlockRepository : BaseRepository<MainBlock>

@Repository
interface TransactionRepository<E : Transaction>: BaseRepository<E> {

    fun findOneByHash(hash: String): E?

    fun findAllByBlockIsNull(): MutableSet<E>

}

@Repository
interface VoteTransactionRepository: TransactionRepository<VoteTransaction>

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): Optional<SeedWord>

}

@Repository
interface StakeholderRepository<E : Stakeholder> : BaseRepository<E> {

    fun findOneByPublicKey(publicKey: String): E?

}

@Repository
interface DelegateRepository : StakeholderRepository<Delegate>