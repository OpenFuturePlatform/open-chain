package io.openfuture.chain.controller

import io.openfuture.chain.domain.node.response.NodeTimestampResponse
import io.openfuture.chain.domain.node.response.NodeVersionResponse
import io.openfuture.chain.property.NodeProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(val nodeProperties: NodeProperties) {

    @GetMapping("/getVersion")
    fun getVersion() = NodeVersionResponse(nodeProperties.version!!)

    @GetMapping("/getTimestamp")
    fun getTimestamp() = NodeTimestampResponse(Date().time)

}