package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
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
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
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

    fun getLast(): BaseBlock

    fun isExists(hash: String): Boolean

    fun getAvgProductionTime(): Long

}

interface GenesisBlockService {

    fun getAll(request: PageRequest): Page<GenesisBlock>

    fun getLast(): GenesisBlock

    fun create(timestamp: Long): GenesisBlockMessage

    fun add(message: GenesisBlockMessage)

    fun isValid(message: GenesisBlockMessage): Boolean

}

interface MainBlockService {

    fun getAll(request: PageRequest): Page<MainBlock>

    fun create(): PendingBlockMessage

    fun add(message: PendingBlockMessage)

    fun isValid(message: PendingBlockMessage): Boolean

    fun synchronize(message: MainBlockMessage)

}

/** Common base transaction service */
interface TransactionService {

    fun getCount(): Long

    fun getAllUnconfirmedByAddress(address: String): List<UnconfirmedTransaction>

    fun getProducingPerSecond(): Long
    
}

interface TransferTransactionService {

    fun getAll(request: PageRequest): Page<TransferTransaction>

    fun getAllUnconfirmed(): MutableList<UnconfirmedTransferTransaction>

    fun getByAddress(address: String): List<TransferTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction

    fun add(message: TransferTransactionMessage): UnconfirmedTransferTransaction

    fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction

    fun synchronize(message: TransferTransactionMessage, block: MainBlock)

    fun toBlock(hash: String, block: MainBlock): TransferTransaction

    fun generateHash(request: TransferTransactionHashRequest): String

}

interface VoteTransactionService {

    fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction

    fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction

    fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction

    fun synchronize(message: VoteTransactionMessage, block: MainBlock)

    fun toBlock(hash: String, block: MainBlock): VoteTransaction

    fun generateHash(request: VoteTransactionHashRequest): String

}

interface DelegateTransactionService {

    fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction

    fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction

    fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction

    fun synchronize(message: DelegateTransactionMessage, block: MainBlock)

    fun toBlock(hash: String, block: MainBlock): DelegateTransaction

    fun generateHash(request: DelegateTransactionHashRequest): String

}

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): List<Delegate>

    fun isExists(key: String): Boolean

    fun save(delegate: Delegate): Delegate

}

interface WalletService {

    fun getByAddress(address: String): Wallet

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun increaseBalance(address: String, amount: Long)

    fun decreaseBalance(address: String, amount: Long)

}