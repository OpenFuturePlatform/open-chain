package io.openfuture.chain.core.service

import io.openfuture.chain.consensus.model.entity.transaction.DelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.domain.application.block.GenesisBlockMessage
import io.openfuture.chain.network.domain.application.block.MainBlockMessage
import io.openfuture.chain.network.domain.application.transaction.BaseTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.VoteTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.data.BaseTransactionData
import io.openfuture.chain.network.domain.application.transaction.data.DelegateTransactionData
import io.openfuture.chain.network.domain.application.transaction.data.TransferTransactionData
import io.openfuture.chain.network.domain.application.transaction.data.VoteTransactionData
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.node.*
import io.openfuture.chain.rpc.domain.transaction.BaseTransactionRequest
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

interface GenesisBlockService {

    fun add(dto: GenesisBlockMessage)

    fun getLast(): GenesisBlock

    fun save(block: GenesisBlock): GenesisBlock

    fun isValid(block: GenesisBlock): Boolean

}

interface MainBlockService {

    fun add(dto: MainBlockMessage)

    fun getLast(): MainBlock

    fun save(block: MainBlock): MainBlock

    fun isValid(block: MainBlock): Boolean

}

interface TransactionService<Entity : Transaction, UEntity : UTransaction> {

    fun toBlock(hash: String, block: MainBlock)

}

interface TransferTransactionService : TransactionService<TransferTransaction, UTransferTransaction> {

    fun toBlock(dto: TransferTransactionMessage, block: MainBlock)

}

interface VoteTransactionService : TransactionService<VoteTransaction, UVoteTransaction> {

    fun toBlock(dto: VoteTransactionMessage, block: MainBlock)

}

interface DelegateTransactionService : TransactionService<DelegateTransaction, UDelegateTransaction> {

    fun toBlock(dto: DelegateTransactionMessage, block: MainBlock)

}

interface UTransactionService<UEntity : UTransaction, Data : BaseTransactionData, Dto : BaseTransactionMessage<Data>,
    Req : BaseTransactionRequest<UEntity, Data>> {

    fun get(hash: String): UEntity

    fun getAll(): MutableSet<UEntity>

    fun add(dto: Dto): UEntity

    fun add(request: Req): UEntity

}

interface UTransferTransactionService : UTransactionService<UTransferTransaction, TransferTransactionData,
    TransferTransactionMessage, TransferTransactionRequest>

interface UVoteTransactionService : UTransactionService<UVoteTransaction, VoteTransactionData,
    VoteTransactionMessage, VoteTransactionRequest>

interface UDelegateTransactionService : UTransactionService<UDelegateTransaction, DelegateTransactionData,
    DelegateTransactionMessage, DelegateTransactionRequest>

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): Set<Delegate>

    fun save(delegate: Delegate): Delegate

}

interface WalletService {

    fun getByAddress(address: String): Wallet

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun updateBalance(from: String, to: String, amount: Long, fee: Long)

}

interface CommonBlockService {

    fun get(hash: String): BaseBlock

    fun getLast(): BaseBlock

    fun getBlocksAfterCurrentHash(hash: String): List<BaseBlock>?

    fun isExists(hash: String): Boolean

}

interface CommonTransactionService {

    fun get(hash: String): Transaction

    fun isExists(hash: String): Boolean

}

interface UCommonTransactionService {

    fun getAll(): MutableSet<UTransaction>

}
