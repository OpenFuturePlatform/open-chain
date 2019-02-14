package io.openfuture.chain.core.service.transaction.validation.pipeline

import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Scope(SCOPE_PROTOTYPE)
@Transactional(readOnly = true)
class RewardTransactionPipelineValidator : TransactionPipelineValidator<RewardTransactionPipelineValidator>() {

    fun check(): RewardTransactionPipelineValidator {
        checkHash()
        checkSignature()
        return this
    }

}