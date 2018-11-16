package io.openfuture.chain.network.component

import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

@Component
class ExplorerAddressesHolder {

    private val nodesInfo = ConcurrentHashMap.newKeySet<NodeInfo>()
    var me: NodeInfo? = null


    fun getNodesInfo(excludeMe: Boolean = true): Set<NodeInfo> {
        if (excludeMe) {
            me?.let { return nodesInfo.filter { nodeInfo -> nodeInfo.uid != it.uid }.toSet() }
        }

        return nodesInfo
    }

    fun addNodeInfo(nodeInfo: NodeInfo) {
        this.nodesInfo.add(nodeInfo)
    }

    fun addNodesInfo(nodesInfo: Set<NodeInfo>) {
        this.nodesInfo.addAll(nodesInfo)
    }

    fun hasNodeInfo(nodeInfo: NodeInfo): Boolean = this.nodesInfo.contains(nodeInfo)

    fun removeNodeInfo(address: NetworkAddress) {
        nodesInfo.removeIf { address == it.address }
    }

    fun getRandomList(listSize: Int): List<NodeInfo> = nodesInfo.shuffled().subList(0, min(nodesInfo.size, listSize))

}