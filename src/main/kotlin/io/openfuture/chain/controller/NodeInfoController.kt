package io.openfuture.chain.controller

import io.openfuture.chain.domain.NodeTimestampResponse
import io.openfuture.chain.domain.NodeVersionResponse
import io.openfuture.chain.service.DefaultNodeInfoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(
        val nodeInfoService: DefaultNodeInfoService
) {

    @GetMapping("/getVersion")
    fun getVersion() = NodeVersionResponse()

    @GetMapping("/getTimestamp")
    fun getTimestamp() = NodeTimestampResponse(System.currentTimeMillis())

    @GetMapping("/getHealthCheck")
    fun getHealthCheck(): Long = nodeInfoService.getUpTime()

}