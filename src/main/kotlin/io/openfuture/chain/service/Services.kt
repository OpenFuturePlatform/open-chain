package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.crypto.key.WalletDto
import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.vote.VoteTransactionData
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.entity.VoteTransaction

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

interface BlockService {

    fun chainSize(): Long

    fun getLast(): Block

    fun add(block: BlockDto): Block

    fun create(privateKey: String, publicKey: String, difficulty: Int): BlockDto

    fun isValid(block: BlockDto): Boolean

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

interface TransactionService<Entity : Transaction> {

    fun getAllPending(): List<Entity>

    fun get(hash: String): Entity

    fun addToBlock(hash: String, block: Block): Entity

}

interface VoteTransactionService : TransactionService<VoteTransaction> {

    fun add(dto: VoteTransactionDto): VoteTransaction

    fun create(data: VoteTransactionData): VoteTransactionDto

}

interface DelegateService {

    fun getAll(): List<Delegate>

    fun getByPublicKey(publicKey: String): Delegate

    fun getActiveDelegates(): List<Delegate>

    fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean

    fun add(dto: DelegateDto): Delegate

}