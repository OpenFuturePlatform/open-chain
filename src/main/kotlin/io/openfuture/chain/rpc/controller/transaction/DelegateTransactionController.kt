package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/delegate")
class DelegateTransactionController(
    private val transactionService: DelegateTransactionService
) {

    @CrossOrigin
    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): DelegateTransactionResponse =
        DelegateTransactionResponse(transactionService.getByHash(hash))

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = transactionService.add(request)
        return DelegateTransactionResponse(tx)
    }

    @GetMapping
    fun sendRequest(): DelegateTransactionResponse {
        val request = DelegateTransactionRequest(
            timestamp = System.currentTimeMillis(),
            amount = 10,
            fee = 3,
            nodeHost = "",
            nodePort = 0,
            nodeId = "",
            nodeKey = "02aef406b4c4a3c007094a05c2d2a2d815133a41914c96385a2d9ca71529b4d302",
            senderAddress = "0x3ecB577F110a7Caaa7B8deE5eE63CFcd2475F1Fd",
            senderPublicKey = "02e9f6ea6d831f496067ea1b2f170f4f103fe7caf9c226d1d43d6741a6aef59a12"
        )
        request.hash = request.createHash()
        request.senderSignature = request.sign("c09255d970f97cfb255a7010ee4fc6f1d330aae03a227b80a8b80145bdb8602c")

        return DelegateTransactionResponse(transactionService.add(request))
    }

}