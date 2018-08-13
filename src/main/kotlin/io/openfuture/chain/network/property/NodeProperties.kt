package io.openfuture.chain.network.property

import io.openfuture.chain.network.message.network.NetworkAddressMessage
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

    /** Node server host */
    @field:NotNull
    var host: String? = null,

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

    /** Inbound connections number */
    @field:NotNull
    @field:Min(5)
    var peersNumber: Int? = null,

    /** Private key path */
    @field:NotNull
    var privateKeyPath: String? = null,

    /** Public key path */
    @field:NotNull
    var publicKeyPath: String? = null,

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

    fun getRootAddresses(): List<NetworkAddressMessage> = rootNodes.map {
        val addressParts = it.split(':')
        NetworkAddressMessage(addressParts[0], addressParts[1].toInt())
    }

}