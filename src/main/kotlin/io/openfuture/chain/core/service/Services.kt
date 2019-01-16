package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.State
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.node.*
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

    fun getAfterCurrentHash(hash: String): List<Block>

    fun isExists(hash: String): Boolean

    fun getAvgProductionTime(): Long

    fun getCurrentHeight(): Long

    fun isExists(hash: String, height: Long): Boolean

}

interface GenesisBlockService {

    fun getByHash(hash: String): GenesisBlock

    fun getAll(request: PageRequest): Page<GenesisBlock>

    fun getLast(): GenesisBlock

    fun getByEpochIndex(epochIndex: Long): GenesisBlock?

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

    fun toBlock(message: TransferTransactionMessage, block: MainBlock): TransferTransaction

    fun verify(message: TransferTransactionMessage): Boolean

}

interface RewardTransactionService {

    fun getAll(request: TransactionPageRequest): Page<RewardTransaction>

    fun getByRecipientAddress(address: String): List<RewardTransaction>

    fun create(timestamp: Long, fees: Long): RewardTransactionMessage

    fun toBlock(message: RewardTransactionMessage, block: MainBlock)

    fun verify(message: RewardTransactionMessage): Boolean

    fun save(transaction: RewardTransaction)

}

interface VoteTransactionService {

    fun getUnconfirmedCount(): Long

    fun getByHash(hash: String): VoteTransaction

    fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedVoteTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction

    fun getUnconfirmedBySenderAgainstDelegate(senderAddress: String, nodeId: String): UnconfirmedVoteTransaction?

    fun getLastVoteForDelegate(senderAddress: String, nodeId: String): VoteTransaction

    fun add(message: VoteTransactionMessage)

    fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction

    fun toBlock(message: VoteTransactionMessage, block: MainBlock): VoteTransaction

    fun verify(message: VoteTransactionMessage): Boolean

}

interface DelegateTransactionService {

    fun getUnconfirmedCount(): Long

    fun getByHash(hash: String): DelegateTransaction

    fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedDelegateTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction

    fun add(message: DelegateTransactionMessage)

    fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction

    fun toBlock(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction

    fun verify(message: DelegateTransactionMessage): Boolean

}

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getByNodeId(nodeId: String): Delegate

    fun getActiveDelegates(): List<Delegate>

    fun isExistsByPublicKey(key: String): Boolean

    fun isExistsByNodeId(nodeId: String): Boolean

    fun isExistsByNodeIds(nodeIds: List<String>): Boolean

    fun save(delegate: Delegate): Delegate

}

interface ViewDelegateService {

    fun getAll(request: PageRequest): Page<ViewDelegate>

    fun getByNodeId(nodeId: String): ViewDelegate

}

interface StateService {

    fun getLastByAddress(address: String): State?

    fun getByAddress(address: String): List<State>

    fun getByAddressAndBlock(address: String, block: Block): State?

    fun getBalanceByAddress(address: String): Long

    fun getActualBalanceByAddress(address: String): Long

    fun increaseBalance(address: String, amount: Long)

    fun decreaseBalance(address: String, amount: Long)

    fun getVotesByAddress(address: String): List<String>

    fun updateVote(address: String, nodeId: String, type: VoteType)

    fun create(state: State): State

}