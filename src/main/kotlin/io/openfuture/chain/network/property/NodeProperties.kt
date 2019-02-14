package io.openfuture.chain.network.property

import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.annotation.PostConstruct
import javax.validation.constraints.*

@Component
@Validated
@ConfigurationProperties(value = "node")
class NodeProperties(

    /** Root nodes list */
    @field:NotEmpty
    @field:Size(min = 1, max = 21)
    var rootNodes: List<String> = emptyList(),

    /** Node server port */
    @field:NotNull
    var port: Int? = null,

    /** Node communication protocol version */
    @field:NotNull
    var protocolVersion: String? = null,

    /** Number of boss threads */
    @field:NotNull
    var bossCount: Int? = null,

    /** Backlog */
    @field:NotNull
    var backlog: Int? = null,

    /** */
    @field:NotNull
    var keepAlive: Boolean? = null,

    /** Connection timeout */
    @field:NotNull
    var connectionTimeout: Int? = null,

    /** Interval of ping pong*/
    @field:NotNull
    var heartBeatInterval: Int? = null,

    /** Inbound connections number */
    @field:NotNull
    @field:Min(2)
    var peersNumber: Int? = null,

    /** Config path */
    @field:NotNull
    var configPath: String? = null,

    /** Interval for triggering node explorer task in milliseconds */
    @field:NotNull
    var explorerInterval: Long? = null,

    /** Max synchronization response delay in milliseconds. Max value 10000 millis*/
    @field:Max(10 * 1000)
    @field:NotNull
    var expiry: Long? = null,

    /** Max epoch synchronization time in milliseconds.*/
    @field:Min(1000)
    @field:NotNull
    var syncExpiry: Long? = null,

    @field:NotNull
    var peerPenalty: Long? = null,

    @field:NotNull
    var syncBatchSize: Int? = null,

    @field:NotEmpty
    var ntpServers: List<String> = emptyList(),

    @field:NotNull
    var ntpOffsetThreshold: Long? = null


) {

    private var allowedConnections: Int? = null
    private var me: NodeInfo? = null
    private var rootNetworkAddresses = mutableSetOf<NetworkAddress>()

    @PostConstruct
    private fun init() {
        allowedConnections = peersNumber!! * 2 + 1

        rootNetworkAddresses = rootNodes.map {
            val addressParts = it.split(':')
            NetworkAddress(addressParts[0], addressParts[1].toInt())
        }.toMutableSet()
    }

    fun getRootAddresses(): Set<NetworkAddress> = rootNetworkAddresses


    fun setMyself(nodeInfo: NodeInfo) {
        me = nodeInfo
        rootNetworkAddresses.remove(me!!.address)
    }

    fun getMe(): NodeInfo? = me

    fun getAllowedConnections(): Int = allowedConnections!!

}