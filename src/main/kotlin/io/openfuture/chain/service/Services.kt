package io.openfuture.chain.service

import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.MinedBlockDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface HardwareInfoService {

    fun getHardwareInfo(): HardwareInfo

    fun getCpuInfo(): CpuInfo

    fun getRamInfo(): RamInfo

    fun getDiskStorageInfo(): List<StorageInfo>

    fun getNetworksInfo(): List<NetworkInfo>

}

interface BlockService {

    fun count(): Long

    fun getAll(): MutableList<Block>

    fun getAll(pageRequest: Pageable): Page<Block>

    fun getLast(): Block

    fun save(dto: MinedBlockDto): Block

}

interface TransactionService {

    fun save(block: Block, dto: TransactionDto): Transaction

}