package io.openfuture.chain.service

import io.netty.channel.Channel
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
import io.openfuture.chain.entity.Peer
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

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

interface PeerService {

    fun findAll() : List<CommunicationProtocol.Peer>

    fun save(address: Peer)

    fun saveAll(addresses: List<CommunicationProtocol.Peer>)

    fun deleteAll()

    fun deleteByNetworkId(networkId: String)

    fun findByNetworkId(networkId: String) : Peer?

}

interface NetworkService {

    @EventListener
    fun start(event: ApplicationReadyEvent)

    fun connect(host: String, port: Int)

    fun broadcast(packet: CommunicationProtocol.Packet)

    fun disconnect(channel: Channel)

    fun activeInboundChannels(): MutableSet<Channel>

    fun activeOutboundChannels(): MutableSet<Channel>

    fun isConnected(networkId: String): Boolean

    fun getNetworkId(): String?

    fun setNetworkId(networkId: String)

    fun maintainInboundConnections()
}