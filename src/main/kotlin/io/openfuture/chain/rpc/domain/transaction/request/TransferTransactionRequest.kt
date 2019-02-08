package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.crypto.annotation.AddressChecksum
import io.openfuture.chain.rpc.validation.annotation.TransferTransaction
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@TransferTransaction
data class TransferTransactionRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var hash: String? = null,
    @field:NotBlank @field:AddressChecksum var senderAddress: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null,
    @field:NotNull var amount: Long? = null,
    @field:AddressChecksum var recipientAddress: String? = null,
    var data: String? = null
)