package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.WalletVote
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
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

    fun findFirstByHeaderSenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeIdOrderByHeaderTimestampDesc(senderAddress: String, delegateKey: String, typeId: Int): VoteTransaction?

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

    fun findOneByHeaderSenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeId(senderAddress: String, delegateKey: String, typeId: Int): UnconfirmedVoteTransaction?

}

@Repository
interface UDelegateTransactionRepository : UTransactionRepository<UnconfirmedDelegateTransaction>

@Repository
interface UTransferTransactionRepository : UTransactionRepository<UnconfirmedTransferTransaction>

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findOneByPublicKey(key: String): Delegate?

    fun existsByPublicKey(key: String): Boolean

    @Query("Select * From DELEGATES Where PUBLIC_KEY In :ids ", nativeQuery = true)
    fun findByPublicKeys(@Param("ids") publicKeys: List<String>): List<Delegate>

}

@Repository
interface StateRepository<T : State> : BaseRepository<T> {

    fun findFirstByAddressOrderByBlockIdDesc(address: String): T?

    fun findByAddress(address: String): List<T>

    @Query("""
        Select * From DELEGATE_STATES DS
        Join STATES S On(DS.ID=S.ID)
        Join BLOCKS B On(S.block_id=B.ID)
        Where B.HEIGHT=:height AND S.ADDRESS=:address
        """,
        nativeQuery = true)
    fun findByAddressAndBlockHeight(@Param("address") address: String, @Param("height") height: Long): T?

}

@Repository
interface DelegateStateRepository : StateRepository<DelegateState> {

    @Query("""
        Select * From DELEGATE_STATES DS1
        Join STATES S1 On(DS1.ID=S1.ID)
        Join BLOCKS B1 On(S1.block_id=B1.ID)
        Where B1.HEIGHT = (
            Select Max(B2.HEIGHT) From DELEGATE_STATES DS2
            Join STATES S2 ON(DS2.ID=S2.ID)
            Join BLOCKS B2 ON(S2.BLOCK_ID=B2.ID)
            Where S1.address=S2.address
        )
        """,
        nativeQuery = true)
    fun findLastAll(): List<DelegateState>

}

@Repository
interface WalletStateRepository : StateRepository<WalletState>

@Repository
interface WalletVoteRepository : BaseRepository<WalletVote> {

    fun findAllByIdAddress(address: String): List<WalletVote>

    fun findAllByIdDelegateKey(delegateKey: String): List<WalletVote>

    fun deleteByIdAddressAndIdDelegateKey(address: String, delegateKey: String)

}