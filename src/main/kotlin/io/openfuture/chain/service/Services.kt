package io.openfuture.chain.service

import io.openfuture.chain.domain.base.PageRequest
import io.openfuture.chain.domain.transaction.*
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.*
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
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

interface BlockService<T : Block> {

    fun getLast(): T

    fun save(block: T): T

    fun isValid(block: T): Boolean

}

interface CommonBlockService {

    fun get(hash: String): Block

    fun getLast(): Block

    fun getBlocksAfterCurrentHash(hash: String): List<Block>?

    fun isExists(hash: String): Boolean

}

interface GenesisBlockService : BlockService<GenesisBlock> {

    fun add(dto: NetworkGenesisBlock)

}

interface MainBlockService : BlockService<MainBlock> {

    fun add(dto: NetworkMainBlock)

}

/**
 * The utility service that is not aware of transaction types, has default implementation
 */
interface CommonTransactionService {

    fun get(hash: String): Transaction

    fun isExists(hash: String): Boolean

}

interface TransactionService<Entity : Transaction, UEntity : UTransaction> {

    fun get(hash: String): UEntity

    fun toBlock(hash: String, block: MainBlock)

}

interface RewardTransactionService {

    fun toBlock(tx: RewardTransaction, block: MainBlock)

    fun toBlock(dto: RewardTransactionDto, block: MainBlock)

}

interface TransferTransactionService : TransactionService<TransferTransaction, UTransferTransaction> {

    fun toBlock(dto: TransferTransactionDto, block: MainBlock)

}

interface VoteTransactionService : TransactionService<VoteTransaction, UVoteTransaction> {

    fun toBlock(dto: VoteTransactionDto, block: MainBlock)

}

interface DelegateTransactionService : TransactionService<DelegateTransaction, UDelegateTransaction> {

    fun toBlock(dto: DelegateTransactionDto, block: MainBlock)

}

/**
 * The utility service that is not aware of transaction types, has default implementation
 */
interface UCommonTransactionService {

    fun getAll(): MutableSet<UTransaction>

}

interface UTransactionService<UEntity : UTransaction, Data : BaseTransactionData, Dto : BaseTransactionDto<Data>,
    Req : BaseTransactionRequest<UEntity, Data>> {

    fun get(hash: String): UEntity

    fun getAll(): MutableSet<UEntity>

    fun add(dto: Dto): UEntity

    fun add(request: Req): UEntity

}

interface UTransferTransactionService : UTransactionService<UTransferTransaction, TransferTransactionData,
    TransferTransactionDto, TransferTransactionRequest>

interface UVoteTransactionService : UTransactionService<UVoteTransaction, VoteTransactionData,
    VoteTransactionDto, VoteTransactionRequest>

interface UDelegateTransactionService : UTransactionService<UDelegateTransaction, DelegateTransactionData,
    DelegateTransactionDto, DelegateTransactionRequest>

interface DelegateService {

    fun getAll(request: PageRequest): Page<Delegate>

    fun getByPublicKey(key: String): Delegate

    fun getActiveDelegates(): Set<Delegate>

    fun save(delegate: Delegate): Delegate

}

interface ConsensusService {

    fun getCurrentEpochHeight(): Long

    fun isGenesisBlockNeeded(): Boolean

}

interface WalletService {

    fun getByAddress(address: String): Wallet

    fun getBalanceByAddress(address: String): Long

    fun getVotesByAddress(address: String): MutableSet<Delegate>

    fun save(wallet: Wallet)

    fun updateBalance(from: String, to: String, amount: Long, fee: Long)

}