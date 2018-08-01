package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>, PagingAndSortingRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<BaseBlock>{

    fun findByHash(hash: String): BaseBlock?

    fun findByHeightGreaterThan(height: Long): List<BaseBlock>?

    fun findFirstByOrderByHeightDesc(): BaseBlock?

    fun existsByHash(hash: String): Boolean

}

@Repository
interface MainBlockRepository : BaseRepository<MainBlock> {

    fun findFirstByOrderByHeightDesc(): MainBlock?

}

@Repository
interface GenesisBlockRepository : BaseRepository<GenesisBlock> {

    fun findFirstByOrderByHeightDesc(): GenesisBlock?

}

@Repository
interface TransactionRepository<Entity : Transaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

}

@Repository
interface TransferTransactionRepository : TransactionRepository<TransferTransaction>

@Repository
interface VoteTransactionRepository : TransactionRepository<VoteTransaction>

@Repository
interface DelegateTransactionRepository : TransactionRepository<DelegateTransaction>

@Repository
interface UTransactionRepository<UEntity : UTransaction> : BaseRepository<UEntity> {

    fun findOneByHash(hash: String): UEntity?

    fun findAllByOrderByFeeDesc(pageable: Pageable): MutableList<UEntity>

}

@Repository
interface UTransferTransactionRepository : UTransactionRepository<UTransferTransaction>

@Repository
interface UVoteTransactionRepository : UTransactionRepository<UVoteTransaction>

@Repository
interface UDelegateTransactionRepository : UTransactionRepository<UDelegateTransaction>

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findOneByPublicKey(key: String): Delegate?

}

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}