package io.openfuture.chain.service

import io.openfuture.chain.entity.Node
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.NodeRepository
import org.springframework.stereotype.Component

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
}