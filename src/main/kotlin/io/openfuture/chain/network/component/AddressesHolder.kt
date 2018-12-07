package io.openfuture.chain.network.component

import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class AddressesHolder(
    private val nodeProperties: NodeProperties,
    private val nodeKeyHolder: NodeKeyHolder
) {

    private val nodesInfo = ConcurrentHashMap<NodeInfo, ConnectionMark>()


    private class ConnectionMark(
        var rejected: Boolean = false,
        var timestamp: Long = System.currentTimeMillis()
    )

    fun getNodesInfo(): Set<NodeInfo> {
        return nodesInfo.keys
    }

    fun addNodeInfo(nodeInfo: NodeInfo) {
        val uid = nodeKeyHolder.getUid()
        if (uid != nodeInfo.uid) {
            this.nodesInfo.putIfAbsent(nodeInfo, ConnectionMark())
        }
    }

    fun addNodesInfo(nodesInfo: Set<NodeInfo>) {
        val uid = nodeKeyHolder.getUid()
        val nodesInfoWithoutMe = nodesInfo.filter { uid != it.uid }
        for (nodeInfo in nodesInfoWithoutMe) {
            this.nodesInfo.putIfAbsent(nodeInfo, ConnectionMark())
        }
    }

    fun removeNodeInfo(address: NetworkAddress) {
        nodesInfo.entries.removeIf { address == it.key.address }
    }

    fun getRandomList(listSize: Int = nodesInfo.size, connectedPeers: List<NodeInfo> = emptyList()): List<NodeInfo> {
        return nodesInfo.filter { !it.value.rejected }.keys.minus(connectedPeers).shuffled().take(listSize)
    }

    fun getRandom(connectedPeers: List<NodeInfo> = emptyList()): NodeInfo {
        return nodesInfo.filter { !it.value.rejected }.keys.minus(connectedPeers).shuffled().first()
    }

    fun size(): Int {
        return nodesInfo.size
    }

    fun hasNodeInfo(nodeInfo: NodeInfo): Boolean = this.nodesInfo.containsKey(nodeInfo)

    fun isRejected(address: NetworkAddress): Boolean {
        this.nodesInfo.entries.find { it.key.address == address }?.let {
            return it.value.rejected
        }
        return nodeProperties.getMe()?.address == address
    }

    fun markRejected(nodeInfo: NodeInfo) {
        val mark = nodesInfo[nodeInfo] ?: return
        mark.rejected = true
        mark.timestamp = System.currentTimeMillis()
    }


    @Scheduled(fixedRateString = "\${node.peer-unavailability-period}")
    fun cancelRejectedStatus() {
        val timeFrom = System.currentTimeMillis() - nodeProperties.peerUnavailabilityPeriod!!
        nodesInfo.filter { it.value.timestamp > timeFrom }.forEach {
            it.value.rejected = false
        }
    }
}