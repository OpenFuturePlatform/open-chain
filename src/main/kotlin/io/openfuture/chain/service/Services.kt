package io.openfuture.chain.service

import io.netty.channel.Channel
import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.domain.rpc.HardwareInfo
import io.openfuture.chain.domain.rpc.crypto.AccountDto
import io.openfuture.chain.domain.rpc.hardware.CpuInfo
import io.openfuture.chain.domain.rpc.hardware.NetworkInfo
import io.openfuture.chain.domain.rpc.hardware.RamInfo
import io.openfuture.chain.domain.rpc.hardware.StorageInfo
import io.openfuture.chain.domain.rpc.transaction.TransactionRequest
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.*
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.network.domain.Packet

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

interface BaseTransactionService<Entity : BaseTransaction> {

    fun getAllPending(): MutableSet<Entity>

    fun get(hash: String): Entity

    fun getByBlock(block: Block): List<Entity>

    fun addToBlock(hash: String, block: MainBlock): Entity

    fun save(entity: Entity): Entity

}

interface TransactionService<Entity : BaseTransaction, Dto : BaseTransactionDto, Req : TransactionRequest> : BaseTransactionService<Entity> {

    fun add(dto: Dto)

    fun add(request: Req)

}

interface TransferTransactionService : TransactionService<TransferTransaction, TransferTransactionDto, TransferTransactionRequest>

interface VoteTransactionService : TransactionService<VoteTransaction, VoteTransactionDto, VoteTransactionRequest>

interface DelegateService {

    fun getByHostAndPort(host: String, port: Int) : Delegate

    fun getActiveDelegates(): Set<Delegate>

    fun add(dto: DelegateDto): Delegate

    fun save(delegate: Delegate): Delegate

}

interface StakeholderService {

    fun getAll(): List<Stakeholder>

    fun getByPublicKey(publicKey: String): Stakeholder

    fun add(dto: StakeholderDto): Stakeholder

    fun save(entity: Stakeholder): Stakeholder

}

interface ConsensusService {

    fun getCurrentEpochHeight(): Long

    fun isGenesisBlockNeeded(): Boolean

}

interface WalletService {

    fun getBalance(address: String): Double

    fun updateByTransaction(transaction: BaseTransaction)

}

interface NetworkService {

    fun broadcast(packet: Packet)

    fun maintainConnectionNumber()

    fun addConnection(channel: Channel, networkAddress: NetworkAddress)

    fun removeConnection(channel: Channel) : NetworkAddress?

    fun getConnections(): Set<NetworkAddress>

    fun connect(peers: List<NetworkAddress>)

}