package io.openfuture.chain.repository

import io.openfuture.chain.entity.SeedWord
import io.openfuture.chain.entity.Stakeholder
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.peer.Delegate
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<Block> {

    fun findByHash(hash: String): Block?

    fun findFirstByOrderByHeightDesc(): Block?

}

@Repository
interface BaseTransactionRepository<Entity : BaseTransaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

    fun findAllByBlockIsNull(): MutableSet<Entity>

    fun existsByHash(hash: String): Boolean

}

@Repository
interface TransferTransactionRepository : BaseTransactionRepository<TransferTransaction>

@Repository
interface VoteTransactionRepository : BaseTransactionRepository<VoteTransaction>

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): SeedWord?

}

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findAllByOrderByRatingDesc(pageable: Pageable): List<Delegate>

    fun findOneByHostAndPort(host: String, port: Int): Delegate?

}

@Repository
interface StakeholderRepository : BaseRepository<Stakeholder> {

    fun findOneByPublicKey(publicKey: String): Stakeholder?

}

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}