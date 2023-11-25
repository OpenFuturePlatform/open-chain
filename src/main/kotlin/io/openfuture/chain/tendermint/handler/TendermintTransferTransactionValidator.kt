package io.openfuture.chain.tendermint.handler

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.util.TendermintTransactionValidateHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TendermintTransferTransactionValidator : TendermintTransactionValidator() {

    fun check(): Array<TendermintTransactionValidateHandler> = arrayOf(
        checkHash(),
        checkSignature(),
        checkSenderAddress(),
        checkNegativeFee(),
        checkNegativeAmount()
    )

    fun checkNew(unconfirmedBalance: Long): Array<TendermintTransactionValidateHandler> = arrayOf(
        *check(),
        //checkActualBalance(unconfirmedBalance)
    )

    fun checkNegativeFee(): TendermintTransactionValidateHandler = {
        if (it.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }
    }

    fun checkNegativeAmount(): TendermintTransactionValidateHandler = {
        it as TendermintTransferTransaction
        if (it.getPayload().amount < 0) {
            throw ValidationException("Amount should not be less than 0")
        }
    }
}