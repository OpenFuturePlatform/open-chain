package io.openfuture.chain.service

import io.openfuture.chain.entity.Peer
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.PeerRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class DefaultPeerService(
    private val repository: PeerRepository
) : PeerService {

    override fun findByNetworkId(networkId: String) : Peer?{
        val node = repository.findOneByNetworkId(networkId)
        return if (node.isPresent) node.get() else null
    }

    override fun save(address: Peer) {
        repository.save(address)
    }

    override fun saveAll(addresses: List<CommunicationProtocol.Peer>) {
        val addressEntities = ArrayList<Peer>(addresses.size)
        addresses.forEach {
            addressEntities.add(Peer(it.networkId, it.host, it.port))
        }
        repository.saveAll(addressEntities)
    }

    override fun findAll(): List<CommunicationProtocol.Peer> {
        val nodes = repository.findAll()
        val result = ArrayList<CommunicationProtocol.Peer>(nodes.size)
        nodes.forEach {
            result.add(CommunicationProtocol.Peer.newBuilder()
                .setNetworkId(it.networkId)
                .setHost(it.host)
                .setPort(it.port)
                .build())
        }
        return result
    }

    @Transactional
    override fun deleteByNetworkId(networkId: String) {
        repository.deleteOneByNetworkId(networkId)
    }

    override fun deleteAll() {
        repository.deleteAll()
    }
}
