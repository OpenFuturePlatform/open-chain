package io.openfuture.chain.service

import io.openfuture.chain.entity.Node
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.NodeRepository
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class DefaultNodeService(
    private val repository: NodeRepository
) : NodeService {
    override fun saveAll(nodes: List<CommunicationProtocol.Node>) {
        val nodeEntities = ArrayList<Node>(nodes.size)
        nodes.forEach {
            nodeEntities.add(Node(it.networkId, it.host, it.port))
        }
        repository.saveAll(nodeEntities)
    }

    override fun save(node: Node) {
        repository.save(node)
    }

    override fun findAll(): List<CommunicationProtocol.Node> {
        val nodes = repository.findAll()
        val result = ArrayList<CommunicationProtocol.Node>(nodes.size)
        nodes.forEach {
            result.add(CommunicationProtocol.Node.newBuilder()
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

    override fun findByNetworkId(networkId: String) : Node?{
        val node = repository.findOneByNetworkId(networkId)
        if (node.isPresent) {
            return node.get()
        }
        return null
    }
}
