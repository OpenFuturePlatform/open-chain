package io.openfuture.chain.rpc.controller.base

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MainController {

    @RequestMapping(value = ["/"])
    fun frontend(): String = "frontend"

}