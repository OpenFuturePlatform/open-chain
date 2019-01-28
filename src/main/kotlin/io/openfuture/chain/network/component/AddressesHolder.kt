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

    fun getNodeInfos(): Set<NodeInfo> {
        return nodesInfo.keys
    }

    fun getNodeInfoByUid(uid: String): NodeInfo? {
        return nodesInfo.keys.find { it.uid == uid }
    }

    fun addNodeInfo(nodeInfo: NodeInfo) {
        val uid = nodeKeyHolder.getPublicKeyAsHexString()
        if (uid != nodeInfo.uid) {
            this.nodesInfo.putIfAbsent(nodeInfo, ConnectionMark())
        }
    }

    fun addNodeInfos(nodesInfo: Set<NodeInfo>) {
        val uid = nodeKeyHolder.getPublicKeyAsHexString()
        val nodesInfoWithoutMe = nodesInfo.filter { uid != it.uid }
        for (nodeInfo in nodesInfoWithoutMe) {
            this.nodesInfo.putIfAbsent(nodeInfo, ConnectionMark())
        }
    }

    fun removeNodeInfo(address: NetworkAddress) {
        nodesInfo.entries.removeIf { address == it.key.address }
    }

    fun getRandomList(listSize: Int = nodesInfo.size, exclude: List<NodeInfo> = emptyList()): List<NodeInfo> {
        return nodesInfo.filter { !it.value.rejected }.keys.minus(exclude).shuffled().take(listSize)
    }

    fun size(): Int {
        return nodesInfo.size
    }

    fun hasNodeInfo(nodeInfo: NodeInfo): Boolean = this.nodesInfo.containsKey(nodeInfo)

    fun markRejected(nodeInfo: NodeInfo) {
        val mark = nodesInfo[nodeInfo] ?: return
        mark.rejected = true
        mark.timestamp = System.currentTimeMillis()
    }


    @Scheduled(fixedRateString = "\${node.peer-penalty}")
    fun cancelRejectedStatus() {
        val timeFrom = System.currentTimeMillis() - nodeProperties.peerPenalty!!
        nodesInfo.filter { it.value.timestamp > timeFrom }.forEach {
            it.value.rejected = false
        }
    }
}