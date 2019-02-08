package io.openfuture.chain.smartcontract.execution

import io.openfuture.chain.core.model.entity.ReceiptResult

data class ExecutionResult(
    var receipt: List<ReceiptResult>,
    var state: String? = null,
    var output: Any? = null
)