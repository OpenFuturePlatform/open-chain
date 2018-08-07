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
    fun getHash(@Valid @RequestBody request: DelegateTransactionHashRequest)  {
        val hash = delegateService.generateHash(request)
        val privateKey = ByteUtils.fromHexString("5a1c4c287177586c1d67699fa97f69a015e058d748e6975f333d1c187a978cb4")
        val publicKey = "02dc9ffe0f5fd1ac5a63b6a990d2f63cc15eecbe6276450fb70090c1d16f9b604b"
        val siangture = SignatureUtils.sign(ByteUtils.fromHexString(hash), privateKey)
        val delegateTransactionRequest = DelegateTransactionRequest(request.timestamp, request.fee, request.senderAddress, request.delegateKey,
            siangture, publicKey)
        delegateService.add(delegateTransactionRequest)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = delegateService.add(request)
        return DelegateTransactionResponse(tx)
    }

}

