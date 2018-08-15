package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.Block
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
import io.openfuture.chain.network.message.core.MainBlockMessage
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

}

interface GenesisBlockService {

    fun getLast(): GenesisBlock

    fun create(timestamp: Long): GenesisBlockMessage

    fun add(message: GenesisBlockMessage)

    fun isValid(message: GenesisBlockMessage): Boolean

}

interface MainBlockService {

    fun create(): MainBlockMessage

    fun add(message: MainBlockMessage)

    fun isValid(message: MainBlockMessage): Boolean

}

/** Common base transaction service */
interface TransactionService {

    fun getCount(): Long

    fun getAllUnconfirmedByAddress(address: String): List<UnconfirmedTransaction>

}

interface TransferTransactionService {

    fun getAll(request: PageRequest): Page<TransferTransaction>

    fun getAllUnconfirmed(): MutableList<UnconfirmedTransferTransaction>

    fun getByAddress(address: String): List<TransferTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction

    fun add(message: TransferTransactionMessage): UnconfirmedTransferTransaction

    fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction

    fun toBlock(message: TransferTransactionMessage, block: MainBlock): TransferTransaction

    fun isValid(message: TransferTransactionMessage): Boolean

}

interface VoteTransactionService {

    fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction

    fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction

    fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction

    fun toBlock(message: VoteTransactionMessage, block: MainBlock): VoteTransaction

    fun isValid(message: VoteTransactionMessage): Boolean

}

interface DelegateTransactionService {

    fun getAllUnconfirmed(): MutableList<UnconfirmedDelegateTransaction>

    fun getUnconfirmedByHash(hash: String): UnconfirmedDelegateTransaction

    fun add(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction

    fun add(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction

    fun toBlock(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction

    fun isValid(message: DelegateTransactionMessage): Boolean

}

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): List<Delegate>

    fun isExistsByPublicKey(key: String): Boolean

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