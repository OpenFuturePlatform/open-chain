package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.CONTRACT_METHOD_NOT_EXISTS
import io.openfuture.chain.core.exception.model.ExceptionType.INVALID_CONTRACT
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.util.TransactionValidateHandler
import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import io.openfuture.chain.smartcontract.model.Abi
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TransferTransactionPipelineValidator(
    private val contractService: ContractService
) : TransactionPipelineValidator() {

    fun check(type: TransferTransactionType): Array<TransactionValidateHandler> = arrayOf(
        checkHash(),
        checkSignature(),
        checkSenderAddress(),
        checkNegativeFee(),
        checkNegativeAmount(),
        *when (type) {
            DEPLOY -> {
                arrayOf(
                    checkEqualFee(),
                    checkByteCode()
                )
            }
            EXECUTE -> {
                arrayOf(
                    checkEqualFee(),
                    checkContractCost(),
                    checkContractMethods()
                )
            }
            FUND -> {
                arrayOf(
                    checkEqualAmount()
                )
            }
        }
    )

    fun checkNew(type: TransferTransactionType): Array<TransactionValidateHandler> = arrayOf(
        *check(type),
        checkActualBalance()
    )

    fun checkNegativeFee(): TransactionValidateHandler = {
        if (it.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }
    }

    fun checkNegativeAmount(): TransactionValidateHandler = {
        it as TransferTransaction
        if (it.getPayload().amount < 0) {
            throw ValidationException("Amount should not be less than 0")
        }
    }

    fun checkEqualFee(): TransactionValidateHandler = {
        it as TransferTransaction
        if (it.getPayload().amount == 0L) {
            throw ValidationException("Amount should not be equal to 0")
        }
    }

    fun checkEqualAmount(): TransactionValidateHandler = {
        it as TransferTransaction
        if (it.getPayload().amount == 0L) {
            throw ValidationException("Amount should not be equal to 0")
        }
    }

    fun checkByteCode(): TransactionValidateHandler = {
        it as TransferTransaction
        if (!SmartContractValidator.validate(ByteUtils.fromHexString(it.getPayload().data!!))) {
            throw ValidationException("Invalid smart contract code", INVALID_CONTRACT)
        }
    }

    fun checkContractCost(): TransactionValidateHandler = {
        it as TransferTransaction
        val contract = contractService.getByAddress(it.getPayload().recipientAddress!!)
        if (contract.cost > it.fee) {
            throw ValidationException("Insufficient funds for smart contract execution")
        }
    }

    fun checkContractMethods(): TransactionValidateHandler = { tx ->
        tx as TransferTransaction
        val contract = contractService.getByAddress(tx.getPayload().recipientAddress!!)
        val methods = Abi.fromJson(contract.abi).abiMethods.map { it.name }
        if (!methods.contains(tx.getPayload().data)) {
            throw ValidationException("Smart contract's method ${tx.getPayload().data} not exists",
                CONTRACT_METHOD_NOT_EXISTS)
        }
    }

}