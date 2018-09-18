package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.node.HardwareInfo
import io.openfuture.chain.core.service.HardwareInfoService
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.rpc.domain.NodeInfoResponse
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@CrossOrigin
@RestController
@RequestMapping("/rpc/info")
class NodeInfoController(
    private val nodeClock: NodeClock,
    private val context: ApplicationContext,
    private val nodeKeyHolder: NodeKeyHolder,
    private val nodeConfigurator: NodeConfigurator,
    private val hardwareInfoService: HardwareInfoService
) {

    @GetMapping
    fun getInfo(): NodeInfoResponse = NodeInfoResponse(
        nodeKeyHolder.getPublicKeyAsHexString(),
        nodeKeyHolder.getUid(),
        nodeConfigurator.getConfig().externalHost,
        nodeConfigurator.getConfig().externalPort
    )

    @GetMapping("/getVersion")
    fun getVersion() { }

    @GetMapping("/getUptime")
    fun getUptime(): Long = nodeClock.nodeTime() - context.startupDate

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): HardwareInfo = hardwareInfoService.getHardwareInfo()

}