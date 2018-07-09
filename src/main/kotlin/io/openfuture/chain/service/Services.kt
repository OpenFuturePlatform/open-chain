package io.openfuture.chain.service

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.crypto.key.WalletDto
import io.openfuture.chain.domain.hardware.CpuInfo
import io.openfuture.chain.domain.hardware.NetworkInfo
import io.openfuture.chain.domain.hardware.RamInfo
import io.openfuture.chain.domain.hardware.StorageInfo
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.NetworkAddress
import io.openfuture.chain.protocol.CommunicationProtocol

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

    fun getMasterKey(seedPhrase: String): ExtendedKey

    fun getDerivationKey(seedPhrase: String, derivationPath: String): ExtendedKey

    fun importKey(key: String): ExtendedKey

    fun importWifKey(wifKey: String): ECKey

    fun serializePublicKey(key: ExtendedKey): String

    fun serializePrivateKey(key: ExtendedKey): String

    fun generateKey(): WalletDto

}

interface TransactionService {

    fun save(request: TransactionRequest)

}

interface NetworkAddressService {

    fun findAll() : List<CommunicationProtocol.NetworkAddress>

    fun save(address: NetworkAddress)

    fun saveAll(addresses: List<CommunicationProtocol.NetworkAddress>)

    fun deleteAll()

    fun deleteByNodeId(nodeId: String)

    fun findByNodeId(nodeId: String) : NetworkAddress?

}

interface NetworkService {

    fun join(host : String, port: Int)

    fun connect(host : String, port: Int)

    fun broadcast(packet: CommunicationProtocol.Packet)

}