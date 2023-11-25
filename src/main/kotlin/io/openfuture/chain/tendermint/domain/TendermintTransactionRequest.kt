package io.openfuture.chain.tendermint.domain

data class TendermintTransactionRequest(
    var timestamp: Long? = null,
    var fee: Long? = null,
    var hash: String? = null,
    var senderAddress: String? = null,
    var senderSignature: String? = null,
    var senderPublicKey: String? = null,
    var amount: Long? = null,
    var recipientAddress: String? = null,
) {
}