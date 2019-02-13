package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.rpc.domain.ReceiptResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
@Validated
@RequestMapping("/rpc")
class ReceiptController(
    private val receiptService: ReceiptService
) {
    @CrossOrigin
    @GetMapping("/transactions/{hash}/receipt")
    fun getTransactionReceipt(@PathVariable hash: String): ReceiptResponse =
        ReceiptResponse(receiptService.getByTransactionHash(hash))

}

