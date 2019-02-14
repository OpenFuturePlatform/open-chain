package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.core.util.TransactionValidateHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RewardTransactionPipelineValidator : TransactionPipelineValidator() {

    fun check(): Array<TransactionValidateHandler> = arrayOf(
        checkHash(),
        checkSignature()
    )

}