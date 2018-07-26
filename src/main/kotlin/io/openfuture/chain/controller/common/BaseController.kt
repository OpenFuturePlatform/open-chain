package io.openfuture.chain.controller.common

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.controller.PathConstant
import io.openfuture.chain.property.NodeProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(PathConstant.RPC)
class BaseController {

    @Autowired
    protected lateinit var nodeClock: NodeClock

    @Autowired
    protected lateinit var nodeProperties: NodeProperties


    protected fun getResponseHeader(): ResponseHeader {
        return ResponseHeader(nodeClock.networkTime(), nodeProperties.version!!)
    }

}