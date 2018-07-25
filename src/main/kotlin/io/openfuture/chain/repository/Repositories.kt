package io.openfuture.chain.repository

import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.SeedWord
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.*
import io.openfuture.chain.entity.transaction.unconfirmed.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

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
interface TransactionRepository<Entity : Transaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

    fun findAllByBlockIsNull(): MutableSet<Entity>

}

@Repository
interface UTransactionRepository<Entity : UTransaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

}

@Repository
interface TransferTransactionRepository : TransactionRepository<TransferTransaction>

@Repository
interface VoteTransactionRepository : TransactionRepository<VoteTransaction>

@Repository
interface DelegateTransactionRepository : TransactionRepository<DelegateTransaction>

@Repository
interface RewardTransactionRepository : TransactionRepository<RewardTransaction>


@Repository
interface UTransferTransactionRepository : UTransactionRepository<UTransferTransaction>

@Repository
interface UVoteTransactionRepository : UTransactionRepository<UVoteTransaction>

@Repository
interface UDelegateTransactionRepository : UTransactionRepository<UDelegateTransaction>

@Repository
interface URewardTransactionRepository : UTransactionRepository<URewardTransaction>


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
