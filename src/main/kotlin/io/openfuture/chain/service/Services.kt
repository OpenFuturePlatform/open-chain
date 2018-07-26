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
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.*
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
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

interface BaseTransactionService<Entity : BaseTransaction, Data: BaseTransactionData> {

    fun getAllPending(): MutableSet<Entity>

    fun get(hash: String): Entity

    fun addToBlock(tx: Entity, block: MainBlock): Entity

    fun add(dto: BaseTransactionDto<Data>): Entity

    fun add(request: BaseTransactionRequest<Data>): Entity

    fun add(data: Data): Entity

}

interface TransferTransactionService : BaseTransactionService<TransferTransaction, TransferTransactionData>

interface VoteTransactionService : BaseTransactionService<VoteTransaction,  VoteTransactionData>

interface DelegateTransactionService : BaseTransactionService<DelegateTransaction, DelegateTransactionData>

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

    fun getBalance(address: String): Double

    fun save(wallet: Wallet)

    fun updateBalance(from: String, to: String, amount: Double)

    fun changeWalletVote(address: String, delegate: Delegate, type: VoteType)

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