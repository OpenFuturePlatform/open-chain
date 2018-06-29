package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/crypto")
class CryptoController(
    private val cryptoService: CryptoService
) {

    @PostMapping("/keys/doImport")
    fun importKey(@RequestBody @Valid request: ImportKeyRequest): AddressKeyDto {
        val importedKey = cryptoService.importKey(request.decodedKey!!)
        return AddressKeyDto(
            cryptoService.serializePublicKey(importedKey),
            if (!importedKey.ecKey.isPrivateEmpty()) cryptoService.serializePrivateKey(importedKey) else null,
            importedKey.ecKey.getAddress()
        )
    }

    @PostMapping("/keys/doImportWif")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): AddressKeyDto = AddressKeyDto(
        cryptoService.importWifKey(request.decodedKey!!)
    )

    @GetMapping("/generate")
    fun generateKey() = cryptoService.generateKey()

}