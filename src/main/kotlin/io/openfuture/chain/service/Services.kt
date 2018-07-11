package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.node.DelegateDto
import io.openfuture.chain.domain.node.PeerDto
import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.vote.VoteDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Stakeholder
import io.openfuture.chain.entity.peer.Delegate
import io.openfuture.chain.entity.peer.Peer
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction

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

    fun getLastGenesis(): Block

}

interface CryptoService {

    fun generateSeedPhrase(): String

    fun generateNewAccount(): RootAccountDto

    fun getRootAccount(seedPhrase: String): RootAccountDto

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

}

interface BaseTransactionService<Entity : BaseTransaction> {

    fun getAllPending(): MutableSet<Entity>

    fun get(hash: String): Entity

    fun addToBlock(hash: String, block: Block): Entity

}

interface TransactionService<Entity : BaseTransaction, Dto : BaseTransactionDto, Data : BaseTransactionData> :
    BaseTransactionService<Entity> {

    fun add(dto: Dto): Entity

    fun create(data: Data): Dto

}

interface TransferTransactionService : TransactionService<TransferTransaction, TransferTransactionDto,
    TransferTransactionData>

interface VoteTransactionService : TransactionService<VoteTransaction, VoteTransactionDto, VoteTransactionData>

interface BasePeerService<Entity : Peer, Dto : PeerDto> {

    fun findByNetworkId(networkId: String) : Entity?

    fun getByNetworkId(networkId: String) : Entity

    fun findAll() : List<Entity>

    fun add(dto: Dto): Entity

    fun addAll(list: List<Dto>)

    fun save(entity: Entity)

    fun deleteAll()

    fun deleteByNetworkId(networkId: String)

}

interface PeerService : BasePeerService<Peer, PeerDto>

interface DelegateService : BasePeerService<Delegate, DelegateDto> {

    fun getActiveDelegates(): List<Delegate>

    fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean

    fun updateDelegateRatingByVote(dto: VoteDto)

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

