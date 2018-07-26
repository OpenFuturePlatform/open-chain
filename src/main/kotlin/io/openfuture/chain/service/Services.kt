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
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.*
import io.openfuture.chain.entity.*
import io.openfuture.chain.entity.transaction.*
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.network.domain.Packet
import org.springframework.data.domain.Page

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

interface BlockService {

    fun get(hash: String): Block

    fun getLast(): Block

    fun getLastMain(): MainBlock

    fun getLastGenesis(): GenesisBlock

    fun getBlocksAfterCurrentHash(hash: String): List<Block>?

    fun isExists(hash: String): Boolean

    fun save(block: MainBlock): MainBlock

    fun save(block: GenesisBlock): GenesisBlock

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
interface BaseTransactionService {

    fun getAllPending(): MutableSet<BaseTransaction>

}

interface CommonTransactionService<Entity : BaseTransaction, Data : BaseTransactionData> {

    fun get(hash: String): Entity

    fun isExists(hash: String): Boolean

    fun getAllPending(): MutableSet<Entity>

    fun toBlock(tx: Entity, block: MainBlock)

    fun toBlock(dto: BaseTransactionDto<Data>, block: MainBlock)

    fun add(dto: BaseTransactionDto<Data>): Entity

}

interface EmbeddedTransactionService<Entity : BaseTransaction, Data : BaseTransactionData> : CommonTransactionService<Entity, Data>

interface ManualTransactionService<Entity : BaseTransaction, Data : BaseTransactionData> : CommonTransactionService<Entity, Data> {

    fun add(request: BaseTransactionRequest<Data>): Entity

}

interface RewardTransactionService : EmbeddedTransactionService<RewardTransaction, RewardTransactionData>

interface TransferTransactionService : ManualTransactionService<TransferTransaction, TransferTransactionData>

interface VoteTransactionService : ManualTransactionService<VoteTransaction, VoteTransactionData>

interface DelegateTransactionService : ManualTransactionService<DelegateTransaction, DelegateTransactionData>

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