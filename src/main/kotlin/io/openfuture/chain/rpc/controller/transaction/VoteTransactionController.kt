package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.vote.VoteTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.VoteTransactionResponse
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/votes")
class VoteTransactionController(
    private val transactionService: VoteTransactionService,
    private val blockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService
) {

    @PostMapping("/doGenerateHash")
    fun getHash(@Valid @RequestBody request: VoteTransactionHashRequest) {
        val hash = transactionService.generateHash(request)
        val privateKey = ByteUtils.fromHexString("22766dee3d95143de6ef8a96d00f8dcc45e6689e75b639aecf0abf06956349d6")
        val publicKey = "0267b144c4da46df0051e0ea61cddd96cb55cdec48611701f6dc0f8fc6133c317c"
        val siangture = SignatureUtils.sign(ByteUtils.fromHexString(hash), privateKey)
        val voteTransactionRequest = VoteTransactionRequest(request.timestamp!!, request.fee!!, request.senderAddress!!,
            request.voteTypeId, request.delegateKey, siangture, publicKey)

        transactionService.add(voteTransactionRequest)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: VoteTransactionRequest): VoteTransactionResponse {
        val tx = transactionService.add(request)
        return VoteTransactionResponse(tx)
    }


    @GetMapping("/create")
    fun createBlock() {
        val create1 = genesisBlockService.create()
        genesisBlockService.add(create1)

//        val create = blockService.create()
//        blockService.add(create)
    }

}