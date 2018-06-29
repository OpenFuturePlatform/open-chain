package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.KeyRequest
import io.openfuture.chain.domain.crypto.MasterKeyRequest
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${PathConstant.RPC}/keys")
class CryptoController(
    val cryptoService: CryptoService
) {

    @PostMapping("/getMasterKey")
    fun getMasterKey(@RequestBody keyRequest: MasterKeyRequest) = cryptoService.getMasterKey(keyRequest.seedPhrase)

    @PostMapping("/getDerivationKey")
    fun getDerivationKey(@RequestBody keyRequest: KeyRequest)
            = cryptoService.getDerivationKey(keyRequest.seedPhrase, keyRequest.derivationPath)

}