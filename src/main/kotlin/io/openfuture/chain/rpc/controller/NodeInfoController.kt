package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.node.HardwareInfo
import io.openfuture.chain.core.service.HardwareInfoService
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.rpc.domain.NodeInfoResponse
import org.apache.commons.lang3.StringUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
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
    private val properties: NodeProperties,
    private val context: ApplicationContext,
    private val nodeKeyHolder: NodeKeyHolder,
    private val hardwareInfoService: HardwareInfoService
) {

    @GetMapping
    fun getInfo(): NodeInfoResponse = NodeInfoResponse(nodeKeyHolder.getPublicKey(),
        nodeKeyHolder.getUid(ByteUtils.fromHexString(nodeKeyHolder.getPublicKey())), StringUtils.EMPTY, properties.port!!)

    @GetMapping("/getVersion")
    fun getVersion() { }

    @GetMapping("/getUptime")
    fun getUptime(): Long = nodeClock.nodeTime() - context.startupDate

    @GetMapping("/getHardwareInfo")
    fun getHardwareInfo(): HardwareInfo = hardwareInfoService.getHardwareInfo()

}