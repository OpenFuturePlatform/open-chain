package io.openfuture.chain.network.property

import io.openfuture.chain.network.entity.NetworkAddress
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.*

@Component
@Validated
@ConfigurationProperties(value = "node")
class NodeProperties(

    /** Node server port */
    @field:NotNull
    var port: Int? = null,

    /** Root nodes list */
    @field:NotEmpty
    @field:Size(min = 1, max = 21)
    var rootNodes: List<String> = emptyList(),

    /** Node communication protocol version */
    @field:NotNull
    var protocolVersion: String? = null,

    /** */
    @field:NotNull
    var bossCount: Int? = null,

    /** */
    @field:NotNull
    var backlog: Int? = null,

    /** */
    @field:NotNull
    var keepAlive: Boolean? = null,

    /** */
    @field:NotNull
    var connectionTimeout: Int? = null,

    /** Interval of ping pong*/
    @field:NotNull
    var heartBeatInterval: Int? = null,

    /** Inbound connections number */
    @field:NotNull
    @field:Min(5)
    var peersNumber: Int? = null,

    /** Config path */
    @field:NotNull
    var configPath: String? = null,

    /** Interval for triggering node explorer task in milliseconds */
    @field:NotNull
    var explorerInterval: Long? = null,

    /** Interval for synchronization blocks in milliseconds */
    @field:NotNull
    var syncInterval: Long? = null,

    /** Interval for time synchronization in milliseconds. Min value 60000 millis*/
    @field:Min(15 * 1000)
    @field:NotNull
    var timeSyncInterval: Long? = null,

    /** Max synchronization response delay in milliseconds. Max value 10000 millis*/
    @field:Max(10 * 1000)
    @field:NotNull
    var expiry: Long? = null,

    /** Max chain synchronization time in milliseconds. Min value 10000 millis*/
    @field:Min(10 * 1000)
    @field:NotNull
    var syncExpiry: Long? = null

) {

    fun getRootAddresses(): Set<NetworkAddress> = rootNodes.map {
        val addressParts = it.split(':')
        NetworkAddress(addressParts[0], addressParts[1].toInt())
    }.toSet()

}