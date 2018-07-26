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
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.*
import io.openfuture.chain.entity.transaction.unconfirmed.*
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.protocol.CommunicationProtocol
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

interface TransactionService<Entity : Transaction, UEntity : UTransaction> {

    fun get(hash: String): Entity

    fun save(tx: Entity): Entity

    fun toBlock(tx: UEntity, block: MainBlock): Entity

}

interface RewardTransactionService : TransactionService<RewardTransaction, URewardTransaction>

interface TransferTransactionService : TransactionService<TransferTransaction, UTransferTransaction>

interface VoteTransactionService : TransactionService<VoteTransaction, UVoteTransaction>

interface DelegateTransactionService : TransactionService<DelegateTransaction, UDelegateTransaction>


interface CommonUTransactionService<Entity : UTransaction, Data : BaseTransactionData> {

    fun get(hash: String): Entity

    fun getAll(): MutableSet<Entity>

    fun add(dto: BaseTransactionDto<Data>): Entity

    fun process(tx: Entity)

}

interface EmbeddedUTransactionService<Entity : UTransaction, Data : BaseTransactionData> : CommonUTransactionService<Entity, Data>

interface ManualUTransactionService<Entity : UTransaction, Data : BaseTransactionData> : CommonUTransactionService<Entity, Data> {

    fun add(request: BaseTransactionRequest<Data>): Entity

}

interface URewardTransactionService : EmbeddedUTransactionService<URewardTransaction, RewardTransactionData>

interface UTransferTransactionService : ManualUTransactionService<UTransferTransaction, TransferTransactionData>

interface UVoteTransactionService : ManualUTransactionService<UVoteTransaction, VoteTransactionData>

interface UDelegateTransactionService : ManualUTransactionService<UDelegateTransaction, DelegateTransactionData>


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

    fun broadcast(packet: CommunicationProtocol.Packet)

    fun maintainConnectionNumber()

    fun addConnection(channel: Channel, networkAddress: NetworkAddress)

    fun removeConnection(channel: Channel): NetworkAddress?

    fun getConnections(): Set<NetworkAddress>

    fun connect(peers: List<CommunicationProtocol.NetworkAddress>)

}