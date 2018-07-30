package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.service.WalletService
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.rpc.controller.base.BaseController
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.ImportKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import io.openfuture.chain.rpc.domain.crypto.key.RestoreRequest
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/accounts")
class AccountController(
    private val cryptoService: CryptoService,
    private val walletService: WalletService
) : BaseController() {

    @GetMapping("/doGenerate")
    fun generateNewAccount(): AccountDto = cryptoService.generateNewAccount()

    @GetMapping("/wallets/{address}/balance")
    fun getBalance(@PathVariable address: String): RestResponse<Long> {
        val body = walletService.getBalanceByAddress(address)
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