package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.ConfirmedVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
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

    fun getProducingSpeed(): Long

    fun getLast(): BaseBlock

    fun isExists(hash: String): Boolean

}

interface GenesisBlockService {

    fun getLast(): GenesisBlock

    fun create(): GenesisBlockMessage

    fun add(message: GenesisBlockMessage)

    fun isValid(message: GenesisBlockMessage): Boolean

}

interface MainBlockService {

    fun create(): PendingBlockMessage

    fun add(message: PendingBlockMessage)

    fun isValid(message: PendingBlockMessage): Boolean

    fun synchronize(message: MainBlockMessage)

}

/** Common base transaction service */
interface TransactionService {

    fun getCount(): Long

    fun getAllUnconfirmed(): MutableList<UTransaction>

    fun getUnconfirmedByHash(hash: String): UTransaction

    fun add(message: BaseTransactionMessage): UTransaction

    fun synchronize(message: BaseTransactionMessage, block: MainBlock)

    fun toBlock(utx: UTransaction, block: MainBlock): Transaction

}

interface TransferTransactionService {

    fun add(message: TransferTransactionMessage): UTransferTransaction

    fun add(request: TransferTransactionRequest): UTransferTransaction

    fun synchronize(message: TransferTransactionMessage, block: MainBlock)

    fun toBlock(utx: UTransferTransaction, block: MainBlock): TransferTransaction

    fun generateHash(request: TransferTransactionHashRequest): String

}

interface VoteTransactionService {

    fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction

    fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction

    fun synchronize(message: VoteTransactionMessage, block: MainBlock)

    fun toBlock(utx: UnconfirmedVoteTransaction, block: MainBlock): ConfirmedVoteTransaction

    fun generateHash(request: VoteTransactionHashRequest): String

}

interface DelegateTransactionService {

    fun add(message: DelegateTransactionMessage): UDelegateTransaction

    fun add(request: DelegateTransactionRequest): UDelegateTransaction

    fun synchronize(message: DelegateTransactionMessage, block: MainBlock)

    fun toBlock(utx: UDelegateTransaction, block: MainBlock): DelegateTransaction

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

    fun getUnspentBalanceByAddress(address: String): Long

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun increaseBalance(address: String, amount: Long)

    fun decreaseBalance(address: String, amount: Long)

    fun decreaseUnconfirmedBalance(address: String, amount: Long)

}