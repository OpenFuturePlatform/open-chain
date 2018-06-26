package io.openfuture.chain.service

import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
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

    fun create(privateKey: String, publicKey: String, difficulty: Int,
               transactions: MutableList<TransactionDto>): BlockDto

    fun isValid(block: BlockDto): Boolean

}

interface TransactionService {

    fun save(block: Block, dto: TransactionDto): Transaction

}