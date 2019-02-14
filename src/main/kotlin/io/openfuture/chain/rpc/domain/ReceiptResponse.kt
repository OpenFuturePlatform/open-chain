package io.openfuture.chain.rpc.domain

import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult

class ReceiptResponse(
    val transactionHash: String,
    val results: List<ReceiptResultResponse>
) {

    constructor(receipt: Receipt) : this(
        receipt.transactionHash,
        receipt.getResults().map { ReceiptResultResponse(it) }
    )

}

data class ReceiptResultResponse(
    val from: String,
    val to: String,
    val amount: Long,
    val data: String? = null,
    val error: String? = null
) {

    constructor(receiptResult: ReceiptResult) : this(
        receiptResult.from,
        receiptResult.to,
        receiptResult.amount,
        receiptResult.data,
        receiptResult.error
    )

}
