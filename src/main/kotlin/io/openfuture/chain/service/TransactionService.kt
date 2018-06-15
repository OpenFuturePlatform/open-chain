package io.openfuture.chain.service

import io.openfuture.chain.domain.transaction.TransactionRequest

/**
 * @author Homza Pavel
 */
interface TransactionService {

    fun save(request: TransactionRequest)

}