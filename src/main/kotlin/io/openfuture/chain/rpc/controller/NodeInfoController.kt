package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.service.HardwareInfoService
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.rpc.domain.node.HardwareInfo
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/rpc/info")
class NodeInfoController(
    private val nodeClock: NodeClock,
    private val context: ApplicationContext,
    private val hardwareInfoService: HardwareInfoService) {

    @GetMapping("/getVersion")
    fun getVersion() { }

    @GetMapping("/getUptime")
    fun getUptime(): Long = nodeClock.nodeTime() - context.startupDate

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): HardwareInfo = hardwareInfoService.getHardwareInfo()

}