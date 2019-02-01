package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.node.*
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import org.springframework.data.domain.Page

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

/** Common block info service */
interface BlockService {

    fun getCount(): Long

    fun getLast(): Block

    fun save(block: Block)

    fun removeEpoch(genesisBlock: GenesisBlock)

    fun saveChunk(blocksChunk: List<Block>, syncMode: SyncMode)

    fun getAfterCurrentHash(hash: String): List<Block>

    fun isExists(hash: String): Boolean

    fun findByHash(hash: String): Block?

    fun getAvgProductionTime(): Long

    fun getCurrentHeight(): Long

    fun isExists(hash: String, height: Long): Boolean

    fun findAllByHeightBetween(beginHeight: Long, endHeight: Long): List<Block>

    fun deleteByHeightIn(heights: List<Long>)

    fun isValidHash(block: Block): Boolean

    fun createHash(timestamp: Long, height: Long, previousHash: String, payload: BlockPayload): ByteArray
}

interface GenesisBlockService {

    fun getByHash(hash: String): GenesisBlock

    fun getAll(request: PageRequest): Page<GenesisBlock>

    fun getLast(): GenesisBlock

    fun findByEpochIndex(epochIndex: Long): GenesisBlock?

    fun create(): GenesisBlock

    fun add(block: GenesisBlock)

    fun getPreviousByHeight(height: Long): GenesisBlock

    fun getNextBlock(hash: String): GenesisBlock

    fun getPreviousBlock(hash: String): GenesisBlock

    fun isGenesisBlockRequired(): Boolean

    fun add(message: GenesisBlockMessage)

}

interface MainBlockService {

    fun getByHash(hash: String): MainBlock

    fun getAll(request: PageRequest): Page<MainBlock>

    fun create(): PendingBlockMessage

    fun add(message: BaseMainBlockMessage)

    fun verify(message: PendingBlockMessage): Boolean

    fun getPreviousBlock(hash: String): MainBlock

    fun getNextBlock(hash: String): MainBlock

    fun getBlocksByEpochIndex(epochIndex: Long): List<MainBlock>

}

/** Common base transaction service */
interface TransactionService {

    fun getCount(): Long

    fun getUnconfirmedTransactionByHash(hash: String): UnconfirmedTransaction

    fun getProducingPerSecond(): Long

    fun deleteBlockTransactions(blockHeights: List<Long>)

    fun createHash(header: TransactionHeader, payload: TransactionPayload): String

}

interface TransferTransactionService {

    fun getUnconfirmedCount(): Long

    fun getByHash(hash: String): TransferTransaction

    fun getAll(request: TransactionPageRequest): Page<TransferTransaction>

    fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedTransferTransaction>

    fun getByAddress(address: String, request: TransactionPageRequest): Page<TransferTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction

    fun add(message: TransferTransactionMessage)

    fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction

    fun commit(transaction: TransferTransaction): TransferTransaction

    fun updateState(message: TransferTransactionMessage)

    fun verify(message: TransferTransactionMessage): Boolean

}

interface RewardTransactionService {

    fun getAll(request: TransactionPageRequest): Page<RewardTransaction>

    fun getByRecipientAddress(address: String): List<RewardTransaction>

    fun create(timestamp: Long, fees: Long): RewardTransactionMessage

    fun commit(transaction: RewardTransaction)

    fun updateState(message: RewardTransactionMessage)

    fun verify(message: RewardTransactionMessage): Boolean

}

interface VoteTransactionService {

    fun getUnconfirmedCount(): Long

    fun getByHash(hash: String): VoteTransaction

    fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedVoteTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction

    fun getUnconfirmedBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction?

    fun getLastVoteForDelegate(senderAddress: String, delegateKey: String): VoteTransaction

    fun add(message: VoteTransactionMessage)

    fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction

    fun commit(transaction: VoteTransaction): VoteTransaction

    fun updateState(message: VoteTransactionMessage)

    fun verify(message: VoteTransactionMessage): Boolean

}

interface DelegateTransactionService {

    fun getUnconfirmedCount(): Long

    fun getByHash(hash: String): DelegateTransaction

    fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedDelegateTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction

    fun add(message: DelegateTransactionMessage)

    fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction

    fun commit(transaction: DelegateTransaction): DelegateTransaction

    fun updateState(message: DelegateTransactionMessage)

    fun verify(message: DelegateTransactionMessage): Boolean

}

interface StateService<T : State> {

    fun getLastByAddress(address: String): T?

    fun getByAddress(address: String): List<T>

    fun getByAddressAndBlock(address: String, block: Block): T?

    fun deleteBlockStates(blockHeights: List<Long>)

}

interface DelegateStateService : StateService<DelegateState> {

    fun getAllDelegates(request: PageRequest): List<DelegateState>

    fun getActiveDelegates(): List<DelegateState>

    fun isExistsByPublicKey(key: String): Boolean

    fun isExistsByPublicKeys(publicKeys: List<String>): Boolean

    fun addDelegate(delegateKey: String, walletAddress: String, createDate: Long): DelegateStateMessage

    fun updateRating(delegateKey: String, amount: Long): DelegateStateMessage

    fun commit(state: DelegateState)

}

interface WalletStateService : StateService<WalletState> {

    fun getBalanceByAddress(address: String): Long

    fun getActualBalanceByAddress(address: String): Long

    fun getVotesForDelegate(delegateKey: String): List<WalletState>

    fun updateBalanceByAddress(address: String, amount: Long): WalletStateMessage

    fun updateVoteByAddress(address: String, delegateKey: String?): WalletStateMessage

    fun commit(state: WalletState)

}