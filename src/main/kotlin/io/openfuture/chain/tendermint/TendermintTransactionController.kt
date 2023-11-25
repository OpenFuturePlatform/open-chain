package io.openfuture.chain.tendermint

import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/tendermint/transactions/transfer")
class TendermintTransactionController {

    @PostMapping
    fun add(@Valid @RequestBody request: TransferTransactionRequest){
        println("called tendermint transfer")
    }
}