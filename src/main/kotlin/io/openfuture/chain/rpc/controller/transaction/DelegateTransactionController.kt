package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/delegates")
class DelegateTransactionController(
    private val delegateService: DelegateTransactionService) {

    @PostMapping("/doGenerateHash")
    fun getHash(@Valid @RequestBody request: DelegateTransactionHashRequest): String {
        return delegateService.generateHash(request)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = delegateService.add(request)
        return DelegateTransactionResponse(tx)
    }

}

