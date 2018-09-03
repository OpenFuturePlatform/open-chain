package io.openfuture.chain.network.property

import io.openfuture.chain.network.entity.NetworkAddress
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

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
    var version: String? = null,

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
    var synchronizationInterval: Long? = null,

    /** Max synchronization response delay in milliseconds */
    @field:NotNull
    var synchronizationResponseDelay: Long? = null

) {

    fun getRootAddresses(): Set<NetworkAddress> = rootNodes.map {
        val addressParts = it.split(':')
        NetworkAddress(addressParts[0], addressParts[1].toInt())
    }.toSet()

}