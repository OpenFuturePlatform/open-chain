package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/keys")
class CryptoController(
    private val cryptoService: CryptoService
) {

    @PostMapping("importKey")
    fun importKey(@RequestBody @Valid request: ImportKeyRequest) = cryptoService.importKey(request.decodedKey!!)

    @PostMapping("importWifKey")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): AddressKeyDto = AddressKeyDto(
        cryptoService.importWifKey(request.decodedKey!!)
    )

}