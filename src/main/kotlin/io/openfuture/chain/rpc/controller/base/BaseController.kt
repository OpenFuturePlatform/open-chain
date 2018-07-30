package io.openfuture.chain.rpc.controller.base

import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.rpc.domain.ResponseHeader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/rpc")
class BaseController {

    @Autowired
    protected lateinit var nodeClock: NodeClock

    @Autowired
    protected lateinit var nodeProperties: NodeProperties


    protected fun getResponseHeader(): ResponseHeader {
        return ResponseHeader(nodeClock.networkTime(), nodeProperties.version!!)
    }

}