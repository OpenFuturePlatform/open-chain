package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.crypto.key.WalletDto
import io.openfuture.chain.domain.delegate.StakeholderDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.entity.account.Delegate
import io.openfuture.chain.entity.transaction.Transaction

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

    fun create(transactions: MutableSet<TransactionDto>): MainBlockDto

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

interface TransactionService {

    fun getAllPending(): MutableSet<Transaction>

    fun get(hash: String): Transaction

    fun addToBlock(hash: String, block: Block): Transaction

    // -- votes
    fun addVote(dto: VoteTransactionDto): Transaction

    fun createVote(data: VoteTransactionData): VoteTransactionDto

}

interface StakeholderService {

    // -- accounts
    fun getAllStakeholders(): List<Stakeholder>

    fun getStakeholderByPublicKey(publicKey: String): Stakeholder

    fun addStakeholder(dto: StakeholderDto): Stakeholder

    // -- delegates
    fun getAllDelegates(): List<Delegate>

    fun getDelegateByPublicKey(publicKey: String): Delegate

    fun getActiveDelegates(): List<Delegate>

    fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean

    fun updateDelegateRatingByVote(dto: VoteDto)

}