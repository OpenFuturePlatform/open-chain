package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.crypto.key.WalletDto
import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.stakeholder.DelegateDto
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.entity.account.Delegate
import io.openfuture.chain.entity.transaction.Transaction
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

    fun getMasterKey(seedPhrase: String): ExtendedKey

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

    fun generateKey(): WalletDto

}

interface BaseTransactionService<E : Transaction> {

    fun getAllPending(): MutableSet<E>

    fun get(hash: String): E

    fun addToBlock(hash: String, block: Block): E

}

interface TransactionService : BaseTransactionService<Transaction>

interface VoteTransactionService : BaseTransactionService<VoteTransaction> {

    fun addVote(dto: VoteTransactionDto): VoteTransaction

    fun createVote(data: VoteTransactionData): VoteTransactionDto

}

interface BaseStakeholderService<E : Stakeholder, D : StakeholderDto> {

    fun getAll(): List<E>

    fun getByPublicKey(publicKey: String): E

    fun add(dto: D): E

    fun save(entity: E): E

}

interface StakeholderService: BaseStakeholderService<Stakeholder, StakeholderDto>

interface DelegateService : BaseStakeholderService<Delegate, DelegateDto>{

    fun getActiveDelegates(): List<Delegate>

    fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean

    fun updateDelegateRatingByVote(dto: VoteDto)

}