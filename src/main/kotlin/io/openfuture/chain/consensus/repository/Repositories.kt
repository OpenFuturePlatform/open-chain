package io.openfuture.chain.consensus.repository

import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.Wallet
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.model.entity.transaction.DelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.RewardTransaction
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.crypto.model.entity.SeedWord
import io.openfuture.chain.entity.transaction.VoteTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>, PagingAndSortingRepository<T, Int>

@Repository
interface MainBlockRepository : BaseRepository<MainBlock> {

    fun findFirstByOrderByHeightDesc(): MainBlock?

}

@Repository
interface GenesisBlockRepository : BaseRepository<GenesisBlock> {

    fun findFirstByOrderByHeightDesc(): GenesisBlock?

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
