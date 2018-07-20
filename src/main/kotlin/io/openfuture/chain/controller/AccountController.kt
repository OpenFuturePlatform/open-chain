package io.openfuture.chain.controller

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.controller.common.BaseController
import io.openfuture.chain.controller.common.RestResponse
import io.openfuture.chain.domain.rpc.crypto.AccountDto
import io.openfuture.chain.domain.rpc.crypto.ValidateAddressRequest
import io.openfuture.chain.domain.rpc.crypto.WalletDto
import io.openfuture.chain.domain.rpc.crypto.key.DerivationKeyRequest
import io.openfuture.chain.domain.rpc.crypto.key.ImportKeyRequest
import io.openfuture.chain.domain.rpc.crypto.key.KeyDto
import io.openfuture.chain.domain.rpc.crypto.key.RestoreRequest
import io.openfuture.chain.property.NodeProperty
import io.openfuture.chain.service.CryptoService
import io.openfuture.chain.service.WalletService
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("${PathConstant.RPC}/accounts")
class AccountController(
    nodeClock: NodeClock,
    nodeProperties: NodeProperty,
    private val cryptoService: CryptoService,
    private val walletService: WalletService
) : BaseController(nodeClock, nodeProperties) {

    @GetMapping("/doGenerate")
    fun generateNewAccount(): AccountDto = cryptoService.generateNewAccount()

    @GetMapping("/wallets/{address}/balance")
    fun getBalance(@PathVariable address: String): RestResponse<Double> {
        val body = walletService.getBalance(address)
        return RestResponse(getResponseHeader(), body)
    }

    @PostMapping("/wallets/validateAddress")
    fun validateAddress(@RequestBody @Valid request: ValidateAddressRequest): ValidateAddressRequest = request

    @PostMapping("/doRestore")
    fun restore(@RequestBody @Valid keyRequest: RestoreRequest): RestResponse<AccountDto> {
        val body = cryptoService.getRootAccount(keyRequest.seedPhrase!!)
        return RestResponse(getResponseHeader(), body)
    }

    @PostMapping("/doDerive")
    fun getDerivationAccount(@RequestBody @Valid keyRequest: DerivationKeyRequest): RestResponse<WalletDto> {
        val key = cryptoService.getDerivationKey(keyRequest.seedPhrase!!, keyRequest.derivationPath!!)
        val publicKey = cryptoService.serializePublicKey(key)
        val privateKey = cryptoService.serializePrivateKey(key)
        val body = WalletDto(KeyDto(publicKey, privateKey), key.ecKey.getAddress())
        return RestResponse(getResponseHeader(), body)
    }

    @PostMapping("/keys/doImport")
    fun importKey(@RequestBody @Valid request: ImportKeyRequest): RestResponse<WalletDto> {
        val key = cryptoService.importKey(request.decodedKey!!)
        val publicKey = cryptoService.serializePublicKey(key)
        val privateKey = if (!key.ecKey.isPrivateEmpty()) cryptoService.serializePrivateKey(key) else null
        val body = WalletDto(KeyDto(publicKey, privateKey), key.ecKey.getAddress())
        return RestResponse(getResponseHeader(), body)
    }

    @PostMapping("/keys/doImportWif")
    fun importWifKey(@RequestBody @Valid request: ImportKeyRequest): RestResponse<WalletDto> {
        val body = WalletDto(cryptoService.importWifKey(request.decodedKey!!))
        return RestResponse(getResponseHeader(), body)
    }

}