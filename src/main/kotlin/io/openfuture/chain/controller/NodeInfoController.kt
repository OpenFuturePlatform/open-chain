package io.openfuture.chain.controller

import io.openfuture.chain.domain.HealthResponse
import io.openfuture.chain.domain.NodeTimestampResponse
import io.openfuture.chain.domain.NodeVersionResponse
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author Yury Shakhau
 */
@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(
        private val context: ApplicationContext
) {

    @GetMapping("/getVersion")
    fun getVersion() = NodeVersionResponse()

    @GetMapping("/getTimestamp")
    fun getTimestamp() = NodeTimestampResponse(System.currentTimeMillis())

    @GetMapping("/getHealthCheck")
    fun getHealthCheck() = HealthResponse(System.currentTimeMillis() - context.startupDate)

}