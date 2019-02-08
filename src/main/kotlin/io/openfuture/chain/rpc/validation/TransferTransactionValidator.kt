package io.openfuture.chain.rpc.validation

import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.rpc.validation.annotation.TransferTransaction
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class TransferTransactionValidator : ConstraintValidator<TransferTransaction, TransferTransactionRequest> {

    override fun isValid(value: TransferTransactionRequest, context: ConstraintValidatorContext?): Boolean =
        null != value.recipientAddress || null != value.data

}