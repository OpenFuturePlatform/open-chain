package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.WalletVote
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.transaction.confirmed.*
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository<Entity : Block> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

    fun findOneByHashAndHeight(hash: String, height: Long): Entity?

    fun findFirstByOrderByHeightDesc(): Entity?

    fun findFirstByHeightLessThanOrderByHeightDesc(height: Long): Entity?

    fun findFirstByHeightGreaterThan(height: Long): Entity?

    fun findAllByHeightGreaterThan(height: Long): List<Entity>

    fun findAllByHeightBetween(beginHeight: Long, endHeight: Long): List<Entity>

    @Query(value = "Select HEIGHT From BLOCKS Order By HEIGHT Desc Limit 1", nativeQuery = true)
    fun getCurrentHeight(): Long

}

@Repository
interface MainBlockRepository : BlockRepository<MainBlock>

@Repository
interface GenesisBlockRepository : BlockRepository<GenesisBlock> {

    fun findOneByPayloadEpochIndex(epochIndex: Long): GenesisBlock?

}

@Repository
interface TransactionRepository<Entity : Transaction> : BaseRepository<Entity> {

    fun findOneByFooterHash(hash: String): Entity?

}

@Repository
interface VoteTransactionRepository : TransactionRepository<VoteTransaction> {

    fun findFirstByHeaderSenderAddressAndPayloadNodeIdAndPayloadVoteTypeIdOrderByHeaderTimestampDesc(senderAddress: String, nodeId: String, typeId: Int): VoteTransaction?

}

@Repository
interface DelegateTransactionRepository : TransactionRepository<DelegateTransaction>

@Repository
interface TransferTransactionRepository : TransactionRepository<TransferTransaction> {

    fun findAllByHeaderSenderAddressOrPayloadRecipientAddress(senderAddress: String, recipientAddress: String, request: Pageable): Page<TransferTransaction>

}

@Repository
interface RewardTransactionRepository : BaseRepository<RewardTransaction> {

    fun findOneByFooterHash(hash: String): RewardTransaction?

    fun findAllByPayloadRecipientAddress(payloadRecipientAddress: String): List<RewardTransaction>

}

@Repository
interface UTransactionRepository<UEntity : UnconfirmedTransaction> : BaseRepository<UEntity> {

    fun findOneByFooterHash(hash: String): UEntity?

    fun findAllByOrderByHeaderFeeDesc(request: Pageable): MutableList<UEntity>

    fun findAllByHeaderSenderAddress(address: String): List<UEntity>

}

@Repository
interface UVoteTransactionRepository : UTransactionRepository<UnconfirmedVoteTransaction> {

    fun findOneByHeaderSenderAddressAndPayloadNodeIdAndPayloadVoteTypeId(senderAddress: String, nodeId: String, typeId: Int): UnconfirmedVoteTransaction?

}

@Repository
interface UDelegateTransactionRepository : UTransactionRepository<UnconfirmedDelegateTransaction>

@Repository
interface UTransferTransactionRepository : UTransactionRepository<UnconfirmedTransferTransaction>

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findOneByPublicKey(key: String): Delegate?

    fun findOneByNodeId(nodeId: String): Delegate?

    fun existsByPublicKey(key: String): Boolean

    fun existsByNodeId(nodeId: String): Boolean

    @Query("Select * From DELEGATES Where NODE_ID In :ids ", nativeQuery = true)
    fun findByNodeIds(@Param("ids") nodeIds: List<String>): List<Delegate>

}

@Repository
interface ViewDelegateRepository : BaseRepository<ViewDelegate> {

    fun findOneByNodeId(nodeId: String): ViewDelegate?

}

@Repository
interface StateRepository<T : State> : BaseRepository<T> {

    fun findLastByAddress(address: String): T?

    fun findByAddress(address: String): List<T>

    fun findByAddressAndBlockId(address: String, blockId: Long): T?

}

@Repository
interface DelegateStateRepository : StateRepository<DelegateState> {

    @Query("Select * From DELEGATE_STATES NS Join STATES S Using(ID) Group By S.NODE_ID Having Max(S.HEIGHT_BLOCK)",
        nativeQuery = true)
    fun findLastAll(): List<DelegateState>

}

@Repository
interface WalletStateRepository : StateRepository<WalletState>

@Repository
interface WalletVoteRepository : BaseRepository<WalletVote> {

    fun findAllByIdAddress(address: String): List<WalletVote>

    fun deleteByIdAddressAndIdNodeId(address: String, nodeId: String)

}