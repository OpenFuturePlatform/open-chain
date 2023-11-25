package io.openfuture.chain.tendermint.handler

import io.openfuture.chain.core.model.entity.tendermint.TendermintTransaction
import io.openfuture.chain.core.util.TendermintTransactionValidateHandler

class TendermintTransactionValidationPipeline(
    val handlers: Array<TendermintTransactionValidateHandler>
) {
    fun invoke(tx: TendermintTransaction) {
        handlers.forEach { it.invoke(tx) }
    }
}