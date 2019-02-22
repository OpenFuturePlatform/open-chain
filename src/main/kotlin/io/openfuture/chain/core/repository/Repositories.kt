package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.TemporaryBlock
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.State
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
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Long>

@Repository
interface BlockRepository<Entity : Block> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

    fun findFirstByOrderByHeightDesc(): Entity

    fun findFirstByHeightLessThanOrderByHeightDesc(height: Long): Entity?

    fun findFirstByHeightGreaterThan(height: Long): Entity?

    fun findAllByHeightGreaterThan(height: Long): List<Entity>

    fun findAllByHeightIn(heights: List<Long>): List<Entity>

    fun deleteAllByHeightIn(heights: List<Long>): List<Entity>

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
interface UTransactionRepository<uT : UnconfirmedTransaction> : BaseRepository<uT> {

    fun findOneByHash(hash: String): uT?

    fun findAllByOrderByFeeDesc(request: Pageable): MutableList<uT>

    fun findAllBySenderAddress(address: String): List<uT>

}

@Repository
interface UDelegateTransactionRepository : UTransactionRepository<UnconfirmedDelegateTransaction>

@Repository
interface UTransferTransactionRepository : UTransactionRepository<UnconfirmedTransferTransaction>

@Repository
interface UVoteTransactionRepository : UTransactionRepository<UnconfirmedVoteTransaction> {

    fun findOneBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeId(
        senderAddress: String,
        delegateKey: String,
        typeId: Int
    ): UnconfirmedVoteTransaction?

}

@Repository
interface TransactionRepository<T : Transaction> : BaseRepository<T> {

    fun findOneByHash(hash: String): T?

    fun countByBlock(block: Block): Long

    fun findAllByBlock(block: Block): List<T>

    fun deleteAllByBlockHeightIn(heights: List<Long>)

}

@Repository
interface DelegateTransactionRepository : TransactionRepository<DelegateTransaction>

@Repository
interface TransferTransactionRepository : TransactionRepository<TransferTransaction> {

    fun findAllBySenderAddressOrPayloadRecipientAddress(
        senderAddress: String,
        recipientAddress: String,
        request: Pageable
    ): Page<TransferTransaction>

}

@Repository
interface VoteTransactionRepository : TransactionRepository<VoteTransaction> {

    fun findFirstBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeIdOrderByTimestampDesc(
        senderAddress: String,
        delegateKey: String,
        typeId: Int
    ): VoteTransaction?

}

@Repository
interface RewardTransactionRepository : TransactionRepository<RewardTransaction> {

    fun findAllByPayloadRecipientAddress(payloadRecipientAddress: String): List<RewardTransaction>

}

@Repository
interface StateRepository<T : State> : BaseRepository<T> {

    fun findOneByAddress(address: String): T?

    fun deleteAllByAddressIn(addresses: List<String>)

}

@Repository
interface DelegateStateRepository : StateRepository<DelegateState>

@Repository
interface AccountStateRepository : StateRepository<AccountState> {

    fun findAllByVoteFor(voteFor: String): List<AccountState>

}

@Repository
interface ContractRepository : BaseRepository<Contract> {

    fun findOneByAddress(address: String): Contract?

    fun findAllByOwner(owner: String): List<Contract>

}

@Repository
interface ReceiptRepository : BaseRepository<Receipt> {

    fun findOneByTransactionHash(hash: String): Receipt?

    fun findAllByBlock(block: Block): List<Receipt>

    fun deleteAllByBlockHeightIn(heights: List<Long>)

}

@Repository
interface TemporaryBlockRepository : BaseRepository<TemporaryBlock> {

    fun findByHeightIn(heights: List<Long>): List<TemporaryBlock>

}