package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
import io.openfuture.chain.core.model.entity.transaction.confirmed.*
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository<Entity : Block> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

    fun findFirstByOrderByHeightDesc(): Entity?

    fun findFirstByHeightLessThanOrderByHeightDesc(height: Long): Entity?

    fun findFirstByHeightGreaterThan(height: Long): Entity?

    fun findAllByHeightGreaterThan(height: Long): List<Entity>

}

@Repository
interface MainBlockRepository : BlockRepository<MainBlock>

@Repository
interface GenesisBlockRepository : BlockRepository<GenesisBlock>

@Repository
interface TransactionRepository<Entity : Transaction> : BaseRepository<Entity> {

    fun findOneByFooterHash(hash: String): Entity?

    fun findAllByHeaderSenderAddress(senderAddress: String): List<Entity>

}

@Repository
interface VoteTransactionRepository : TransactionRepository<VoteTransaction>

@Repository
interface DelegateTransactionRepository : TransactionRepository<DelegateTransaction>

@Repository
interface TransferTransactionRepository : TransactionRepository<TransferTransaction> {

    fun findAllByPayloadRecipientAddress(payloadRecipientAddress: String): List<TransferTransaction>

}

@Repository
interface RewardTransactionRepository : BaseRepository<RewardTransaction> {

    fun findOneByFooterHash(hash: String): RewardTransaction?

    fun findAllByPayloadRecipientAddress(payloadRecipientAddress: String): List<RewardTransaction>

}

@Repository
interface UTransactionRepository<UEntity : UnconfirmedTransaction> : BaseRepository<UEntity> {

    fun findOneByFooterHash(hash: String): UEntity?

    fun findAllByOrderByHeaderFeeDesc(): MutableList<UEntity>

    fun findAllByHeaderSenderAddress(address: String): List<UEntity>

}

@Repository
interface UVoteTransactionRepository : UTransactionRepository<UnconfirmedVoteTransaction>

@Repository
interface UDelegateTransactionRepository : UTransactionRepository<UnconfirmedDelegateTransaction>

@Repository
interface UTransferTransactionRepository : UTransactionRepository<UnconfirmedTransferTransaction>

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findOneByPublicKey(key: String): Delegate?

    fun existsByPublicKey(key: String): Boolean

    fun existsByAddress(address: String): Boolean

}

@Repository
interface ViewDelegateRepository : BaseRepository<ViewDelegate>

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}