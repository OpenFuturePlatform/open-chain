package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.rpc.domain.base.PageRequest

class TransactionPageRequest : PageRequest(maySortBy = mapOf("id" to "id", "timestamp" to "timestamp"))