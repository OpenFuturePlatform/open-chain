package io.openfuture.chain.network.component

import io.openfuture.chain.network.entity.NodeInfo
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ExplorerAddressesHolder {

    private val nodesInfo = ConcurrentHashMap.newKeySet<NodeInfo>()
    var me: NodeInfo? = null


    fun getNodesInfo(): Set<NodeInfo> {
        me?.let { return nodesInfo.filter { nodeInfo -> nodeInfo.uid != it.uid }.toSet() }
        return nodesInfo
    }

    @Synchronized
    fun addNodeInfo(nodeInfo: NodeInfo) {
        this.nodesInfo.add(nodeInfo)
    }

    @Synchronized
    fun addNodesInfo(nodesInfo: Set<NodeInfo>) {
        this.nodesInfo.addAll(nodesInfo)
    }

    @Synchronized
    fun removeNodeInfo(nodeInfo: NodeInfo) {
        nodesInfo.remove(nodeInfo)
    }

}