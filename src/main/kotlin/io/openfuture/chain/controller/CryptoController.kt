package io.openfuture.chain.controller

import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.domain.crypto.KeyRequest
import io.openfuture.chain.domain.crypto.KeyResponse
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("${PathConstant.RPC}/keys")
class CryptoController(
    val cryptoService: CryptoService,
    val extendedKeySerializer: ExtendedKeySerializer
) {

    @GetMapping("/getMasterKey")
    fun getMasterKey(
            @RequestParam seedPhrase: String
    ): KeyResponse {
        val extendedKey = cryptoService.getMasterKey(seedPhrase)

        return KeyResponse(
                extendedKeySerializer.serializePublic(extendedKey),
                extendedKeySerializer.serializePrivate(extendedKey),
                extendedKey.ecKey.getAddress()
        )
    }

    @PostMapping("/getKey")
    fun getKey(
            @RequestBody keyRequest: KeyRequest
    ): KeyResponse {
        val extendedKey = cryptoService.getDerivationKey(keyRequest.seedPhrase, keyRequest.derivationPath)

        return KeyResponse(
                extendedKeySerializer.serializePublic(extendedKey),
                extendedKeySerializer.serializePrivate(extendedKey),
                extendedKey.ecKey.getAddress()
        )
    }

}