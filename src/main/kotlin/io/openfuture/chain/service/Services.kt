package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.crypto.key.WalletDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.transaction.TransactionData
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction

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

interface TransactionService {

    fun get(hash: String): Transaction

    fun getAllPending(): List<TransactionDto>

    fun add(dto: TransactionDto): Transaction

    fun create(data: TransactionData): TransactionDto

    fun addToBlock(hash: String, block: Block): Transaction

    fun isValid(dto: TransactionDto): Boolean

    fun isExists(hash: String): Boolean

}

interface VoteTransactionService {

    fun save(request: TransactionRequest)

}