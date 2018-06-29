package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Block

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

    fun save(request: BlockRequest): Block

}

interface CryptoService {

    fun generateSeedPhrase(): String

    fun importKey(key: String): AddressKeyDto

    fun importWifKey(wifKey: String): ECKey

}

interface TransactionService {

    fun save(request: TransactionRequest)

}