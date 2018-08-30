package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
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
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
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

    fun getAfterCurrentHash(hash: String): List<Block>

    fun isExists(hash: String): Boolean

    fun getAvgProductionTime(): Long

}

interface GenesisBlockService {

    fun getByHash(hash: String): GenesisBlock

    fun getAll(request: PageRequest): Page<GenesisBlock>

    fun getLast(): GenesisBlock

    fun create(timestamp: Long): GenesisBlockMessage

    fun add(message: GenesisBlockMessage)

    fun verify(message: GenesisBlockMessage): Boolean

    fun getPreviousByHeight(height: Long): GenesisBlock

    fun getNextBlock(hash: String): GenesisBlock

    fun getPreviousBlock(hash: String): GenesisBlock

}

interface MainBlockService {

    fun getByHash(hash: String): MainBlock

    fun getAll(request: PageRequest): Page<MainBlock>

    fun create(): PendingBlockMessage

    fun add(message: PendingBlockMessage)

    fun add(message: MainBlockMessage)

    fun verify(message: PendingBlockMessage): Boolean

    fun getPreviousBlock(hash: String): MainBlock

    fun getNextBlock(hash: String): MainBlock
}

/** Common base transaction service */
interface TransactionService {

    fun getCount(): Long

    fun getAllUnconfirmedByAddress(address: String): List<UnconfirmedTransaction>

    fun getUnconfirmedTransactionByHash(hash: String): UnconfirmedTransaction

    fun getProducingPerSecond(): Long

}

interface TransferTransactionService {

    fun getByHash(hash: String): TransferTransaction

    fun getAll(request: PageRequest): Page<TransferTransaction>

    fun getAllUnconfirmed(): MutableList<UnconfirmedTransferTransaction>

    fun getByAddress(address: String): List<TransferTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction

    fun add(message: TransferTransactionMessage): UnconfirmedTransferTransaction

    fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction

    fun toBlock(message: TransferTransactionMessage, block: MainBlock): TransferTransaction

    fun verify(message: TransferTransactionMessage): Boolean

}

interface RewardTransactionService {

    fun getAll(request: PageRequest): Page<RewardTransaction>

    fun getByRecipientAddress(address: String): List<RewardTransaction>

    fun create(timestamp: Long, fees: Long): RewardTransactionMessage

    fun toBlock(message: RewardTransactionMessage, block: MainBlock)

    fun verify(message: RewardTransactionMessage): Boolean

}

interface VoteTransactionService {

    fun getByHash(hash: String): VoteTransaction

    fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction

    fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction

    fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction

    fun toBlock(message: VoteTransactionMessage, block: MainBlock): VoteTransaction

    fun verify(message: VoteTransactionMessage): Boolean

}

interface DelegateTransactionService {

    fun getByHash(hash: String): DelegateTransaction

    fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction

    fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction

    fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction

    fun toBlock(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction

    fun verify(message: DelegateTransactionMessage): Boolean

}

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): List<Delegate>

    fun isExistsByPublicKey(key: String): Boolean

    fun isExistsByNodeId(nodeId: String): Boolean

    fun save(delegate: Delegate): Delegate

}

interface ViewDelegateService {

    fun getAll(request: PageRequest): Page<ViewDelegate>

    fun getByNodeId(nodeId: String): ViewDelegate

}

interface WalletService {

    fun getByAddress(address: String): Wallet

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun increaseBalance(address: String, amount: Long)

    fun decreaseBalance(address: String, amount: Long)

    fun increaseUnconfirmedOutput(address: String, amount: Long)

}