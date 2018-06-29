package io.openfuture.chain.controller

import io.openfuture.chain.domain.crypto.DerivationKeyRequest
import io.openfuture.chain.domain.crypto.MasterKeyRequest
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.KeyDto
import io.openfuture.chain.service.CryptoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("${PathConstant.RPC}/keys")
class CryptoController(
    val cryptoService: CryptoService
) {

    @PostMapping("/doGenerateMaster")
    fun getMasterKey(@RequestBody keyRequest: MasterKeyRequest): KeyDto {
        val key = cryptoService.getMasterKey(keyRequest.seedPhrase)

        return KeyDto(
                cryptoService.serializedPublicKey(key),
                cryptoService.serializedPrivateKey(key)
        )
    }

    @PostMapping("/doDerive")
    fun getDerivationKey(@RequestBody keyRequest: DerivationKeyRequest): AddressKeyDto {
        val key = cryptoService.getDerivationKey(keyRequest.seedPhrase, keyRequest.derivationPath)

        return AddressKeyDto(
                cryptoService.serializedPublicKey(key),
                cryptoService.serializedPrivateKey(key),
                key.ecKey.getAddress()
        )
    }

    @GetMapping("/generate")
    fun generateKey() = cryptoService.generateKey()

}