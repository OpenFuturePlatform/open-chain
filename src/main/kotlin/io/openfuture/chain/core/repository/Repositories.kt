package io.openfuture.chain.core.repository

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

    @Query(value = "SELECT height FROM blocks ORDER BY height DESC LIMIT 1", nativeQuery = true)
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
interface StateRepository<T : State> : BaseRepository<T> {

    fun findFirstByAddressOrderByBlockIdDesc(address: String): T?

    fun findByAddress(address: String): List<T>

    fun findFirstByAddressAndBlockHeightLessThanEqualOrderByBlockHeightDesc(address: String, height: Long): T?

}

@Repository
interface DelegateStateRepository : StateRepository<DelegateState> {

    @Query("""
        SELECT ds1 FROM DelegateState ds1
        WHERE ds1.block.height = (
            SELECT MAX(ds2.block.height) FROM DelegateState ds2
            WHERE ds1.address=ds2.address
        )
        """)
    fun findLastAll(request: Pageable): List<DelegateState>

}

@Repository
interface WalletStateRepository : StateRepository<WalletState> {

    @Query("""
        SELECT ws1 FROM WalletState ws1
        WHERE ws1.voteFor=:delegateKey
        AND ws1.block.height = (
            SELECT MAX(ws2.block.height) FROM WalletState ws2
            WHERE ws1.address=ws2.address
        )
        """)
    fun findVotesByDelegateKey(@Param("delegateKey") delegateKey: String): List<WalletState>

}