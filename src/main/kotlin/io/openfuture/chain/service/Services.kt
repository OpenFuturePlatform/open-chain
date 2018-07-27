package io.openfuture.chain.service

import io.netty.channel.Channel
import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.base.PageRequest
import io.openfuture.chain.domain.rpc.HardwareInfo
import io.openfuture.chain.domain.rpc.crypto.AccountDto
import io.openfuture.chain.domain.rpc.hardware.CpuInfo
import io.openfuture.chain.domain.rpc.hardware.NetworkInfo
import io.openfuture.chain.domain.rpc.hardware.RamInfo
import io.openfuture.chain.domain.rpc.hardware.StorageInfo
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.DelegateTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
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
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.network.domain.Packet
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

interface CryptoService {

    fun generateSeedPhrase(): String

    fun generateNewAccount(): AccountDto

    fun getRootAccount(seedPhrase: String): AccountDto

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

}

/**
 * The utility service that is not aware of transaction types, has default implementation
 */
interface BaseUTransactionService {

    fun getPending(): MutableSet<UTransaction>

}

interface BaseTransactionService {

    fun get(hash: String): Transaction

    fun isExists(hash: String): Boolean

}

interface TransactionService<Entity : Transaction, UEntity : UTransaction, Data : BaseTransactionData,
    Dto : BaseTransactionDto<Entity, UEntity, Data>> {

    fun toBlock(tx: Entity, block: MainBlock)

    fun toBlock(dto: Dto, block: MainBlock)

}

interface RewardTransactionService {

    fun toBlock(tx: RewardTransaction, block: MainBlock)

    fun toBlock(dto: RewardTransactionDto, block: MainBlock)

}

interface TransferTransactionService : TransactionService<TransferTransaction, UTransferTransaction,
    TransferTransactionData, TransferTransactionDto>

interface VoteTransactionService : TransactionService<VoteTransaction, UVoteTransaction,
    VoteTransactionData, VoteTransactionDto>

interface DelegateTransactionService : TransactionService<DelegateTransaction, UDelegateTransaction,
    DelegateTransactionData, DelegateTransactionDto>


interface UTransactionService<Entity : Transaction, UEntity : UTransaction, Data : BaseTransactionData,
    Dto : BaseTransactionDto<Entity, UEntity, Data>, Req : BaseTransactionRequest<UEntity, Data>> {

    fun get(hash: String): UEntity

    fun getAll(): MutableSet<UEntity>

    fun add(dto: Dto): UEntity

    fun add(request: Req): UEntity

}

interface UTransferTransactionService : UTransactionService<TransferTransaction, UTransferTransaction, TransferTransactionData,
    TransferTransactionDto, TransferTransactionRequest>

interface UVoteTransactionService : UTransactionService<VoteTransaction, UVoteTransaction, VoteTransactionData,
    VoteTransactionDto, VoteTransactionRequest>

interface UDelegateTransactionService : UTransactionService<DelegateTransaction, UDelegateTransaction, DelegateTransactionData,
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

interface NetworkService {

    fun broadcast(packet: Packet)

    fun maintainConnectionNumber()

    fun connect(peers: List<NetworkAddress>)

}

interface ConnectionService {

    fun addConnection(channel: Channel, networkAddress: NetworkAddress)

    fun removeConnection(channel: Channel): NetworkAddress?

    fun getConnectionAddresses(): Set<NetworkAddress>

    fun getConnections(): MutableMap<Channel, NetworkAddress>

}