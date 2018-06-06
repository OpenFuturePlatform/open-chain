package io.openfuture.chain.controller

import io.openfuture.chain.domain.NodeHardwareResponse
import io.openfuture.chain.domain.NodeVersionResponse
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.sevice.HardwareInfoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(
        val hardwareInfoService: HardwareInfoService,
        val nodeProperties: NodeProperties
) {

    @GetMapping("/getVersion")
    fun getVersion() = NodeVersionResponse(nodeProperties.version!!)

    @GetMapping("/getHardware")
    fun getHardware(): NodeHardwareResponse {
        return hardwareInfoService.getHardwareInfo()
    }

}