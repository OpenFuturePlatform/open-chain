package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.util.TransactionValidateHandler

class TransactionValidationPipeline(
    val handlers: Array<TransactionValidateHandler>
) {

    fun invoke(tx: Transaction) {
        handlers.forEach { it.invoke(tx) }
    }

}