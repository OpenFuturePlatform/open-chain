package io.openfuture.chain.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Component
@Validated
@ConfigurationProperties(value = "node")
class NodeProperties(

        /** Node Server Port */
        @field:NotNull
        var port: Int? = null,

        /** Root Nodes List */
        @field:NotEmpty
        @field:Size(min = 1, max = 8)
        var rootNodes: List<String> = emptyList(),

        /** Node Communication Protocol Version */
        @field:NotNull
        var version: String? = null,

        /** Node delay in seconds wait time before server will be started */
        @field:NotNull
        var pingTime: Int? = null,

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
        var connectTimeout: Int? = null,

        /** Time synchronization interval in milliseconds. Time synchronization will be executed every
         *  interval value. If it is 1000 than synchronization will be every second. */
        @field:NotNull
        var timeSyncInterval: Long? = null

)