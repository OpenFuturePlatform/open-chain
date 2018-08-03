package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.network.message.application.block.BlockMessage
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.node.*
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
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

    fun getLast(): BlockMessage

    fun getBlocksAfterCurrentHash(hash: String): List<BlockMessage>

}

interface GenesisBlockService {

    fun getLast(): GenesisBlock

    fun create(): GenesisBlockMessage

    fun add(dto: GenesisBlockMessage)

    fun isValid(block: GenesisBlockMessage): Boolean

}

interface MainBlockService {

    fun create(): MainBlockMessage

    fun add(dto: MainBlockMessage)

    fun isValid(block: MainBlockMessage): Boolean

}

/** Common base transaction service */
interface TransactionService {

    fun getCount(): Long

}

interface TransferTransactionService {

    fun getAllUnconfirmed(): MutableList<UTransferTransaction>

    fun add(dto: TransferTransactionMessage): UTransferTransaction

    fun add(request: TransferTransactionRequest): UTransferTransaction

    fun toBlock(hash: String, block: MainBlock)

}

interface VoteTransactionService {

    fun getAllUnconfirmed(): MutableList<UVoteTransaction>

    fun add(dto: VoteTransactionMessage): UVoteTransaction

    fun add(request: VoteTransactionRequest): UVoteTransaction

    fun toBlock(hash: String, block: MainBlock)

}

interface DelegateTransactionService {

    fun getAllUnconfirmed(): MutableList<UDelegateTransaction>

    fun add(dto: DelegateTransactionMessage): UDelegateTransaction

    fun add(request: DelegateTransactionRequest): UDelegateTransaction

    fun toBlock(hash: String, block: MainBlock)

}

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): Set<Delegate>

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