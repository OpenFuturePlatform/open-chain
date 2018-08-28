package io.openfuture.chain.rpc.controller.base

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping

@CrossOrigin
@Controller
class MainController {

    @RequestMapping(value = ["/", "/login", "/new", "/new/**", "/upload", "/private-key", "/wallet", "/wallet/**"])
    fun frontend(): String = "frontend"

}