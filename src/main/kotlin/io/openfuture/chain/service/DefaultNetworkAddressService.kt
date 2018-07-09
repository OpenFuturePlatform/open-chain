package io.openfuture.chain.service

import io.openfuture.chain.entity.NetworkAddress
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.NetworkAddressRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class DefaultNetworkAddressService(
    private val repository: NetworkAddressRepository
) : NetworkAddressService {

    override fun findByNodeId(nodeId: String) : NetworkAddress?{
        val node = repository.findOneByNodeId(nodeId)
        return if (node.isPresent) node.get() else null
    }

    override fun save(address: NetworkAddress) {
        repository.save(address)
    }

    override fun saveAll(addresses: List<CommunicationProtocol.NetworkAddress>) {
        val addressEntities = ArrayList<NetworkAddress>(addresses.size)
        addresses.forEach {
            addressEntities.add(NetworkAddress(it.nodeId, it.host, it.port))
        }
        repository.saveAll(addressEntities)
    }

    override fun findAll(): List<CommunicationProtocol.NetworkAddress> {
        val nodes = repository.findAll()
        val result = ArrayList<CommunicationProtocol.NetworkAddress>(nodes.size)
        nodes.forEach {
            result.add(CommunicationProtocol.NetworkAddress.newBuilder()
                .setNodeId(it.nodeId)
                .setHost(it.host)
                .setPort(it.port)
                .build())
        }
        return result
    }

    @Transactional
    override fun deleteByNodeId(nodeId: String) {
        repository.deleteOneByNodeId(nodeId)
    }

    override fun deleteAll() {
        repository.deleteAll()
    }
}
