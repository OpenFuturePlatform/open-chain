package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.stakeholder.DelegateDto
import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.TransactionData
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.vote.VoteDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.entity.account.Delegate
import io.openfuture.chain.entity.transaction.Transaction
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

    fun get(id: Int): Block

    fun getAll(): MutableList<Block>

    fun getLast(): Block?

    fun add(dto: MainBlockDto): Block

    fun create(transactions: MutableSet<out TransactionDto>): MainBlockDto

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

interface BaseTransactionService<Entity : Transaction> {

    fun getAllPending(): MutableSet<Entity>

    fun get(hash: String): Entity

    fun addToBlock(hash: String, block: Block): Entity

}

interface TransactionService<Entity : Transaction, Dto : TransactionDto, Data : TransactionData> :
    BaseTransactionService<Entity> {

    fun add(dto: Dto): Entity

    fun create(data: Data): Dto

}

interface TransferTransactionService : TransactionService<TransferTransaction, TransferTransactionDto,
    TransferTransactionData>

interface VoteTransactionService : TransactionService<VoteTransaction, VoteTransactionDto, VoteTransactionData>

interface BaseStakeholderService<Entity : Stakeholder, Dto : StakeholderDto> {

    fun getAll(): List<Entity>

    fun getByPublicKey(publicKey: String): Entity

    fun add(dto: Dto): Entity

    fun save(entity: Entity): Entity

}

interface StakeholderService : BaseStakeholderService<Stakeholder, StakeholderDto>

interface DelegateService : BaseStakeholderService<Delegate, DelegateDto> {

    fun getActiveDelegates(): List<Delegate>

    fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean

    fun updateDelegateRatingByVote(dto: VoteDto)

}