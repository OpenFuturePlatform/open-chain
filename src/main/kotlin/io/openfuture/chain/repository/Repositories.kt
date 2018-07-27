package io.openfuture.chain.repository

import io.openfuture.chain.entity.*
import io.openfuture.chain.entity.transaction.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>, PagingAndSortingRepository<T, Int>

@Repository
interface BlockRepository<T : Block> : BaseRepository<T> {

    fun findByHash(hash: String): T?

    fun findFirstByOrderByHeightDesc(): T?

}

@Repository
interface MainBlockRepository : BlockRepository<MainBlock>

@Repository
interface GenesisBlockRepository : BlockRepository<GenesisBlock>

@Repository
interface BaseTransactionRepository<Entity : BaseTransaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

    fun findAllByBlockIsNull(): MutableSet<Entity>

}

@Repository
interface TransferTransactionRepository : BaseTransactionRepository<TransferTransaction>

@Repository
interface VoteTransactionRepository : BaseTransactionRepository<VoteTransaction>

@Repository
interface DelegateTransactionRepository : BaseTransactionRepository<DelegateTransaction>

@Repository
interface RewardTransactionRepository : BaseTransactionRepository<RewardTransaction>

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): SeedWord?

}

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findOneByPublicKey(key: String): Delegate?

}

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}
