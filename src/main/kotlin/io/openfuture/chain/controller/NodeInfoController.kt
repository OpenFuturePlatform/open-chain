package io.openfuture.chain.controller

import io.openfuture.chain.domain.HardwareInfo
import io.openfuture.chain.domain.UptimeResponse
import io.openfuture.chain.domain.node.NodeTimestampResponse
import io.openfuture.chain.domain.node.NodeVersionResponse
import io.openfuture.chain.service.HardwareInfoService
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("${PathConstant.RPC}/info")
class NodeInfoController(
    private val context: ApplicationContext,
    val hardwareInfoService: HardwareInfoService
) {

    @GetMapping("/getVersion")
    fun getVersion() = NodeVersionResponse()

    @GetMapping("/getTimestamp")
    fun getTimestamp() = NodeTimestampResponse(System.currentTimeMillis())

    @GetMapping("/getUptime")
    fun getUptime() = UptimeResponse(System.currentTimeMillis() - context.startupDate)

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): HardwareInfo = hardwareInfoService.getHardwareInfo()

}