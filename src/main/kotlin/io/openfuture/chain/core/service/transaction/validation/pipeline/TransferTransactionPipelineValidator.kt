package io.openfuture.chain.core.service.transaction.validation.pipeline

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.CONTRACT_METHOD_NOT_EXISTS
import io.openfuture.chain.core.exception.model.ExceptionType.INVALID_CONTRACT
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import io.openfuture.chain.smartcontract.model.Abi
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Scope(SCOPE_PROTOTYPE)
@Transactional(readOnly = true)
class TransferTransactionPipelineValidator(
    private val contractService: ContractService
) : TransactionPipelineValidator<TransferTransactionPipelineValidator>() {

    fun check(type: TransferTransactionType): TransferTransactionPipelineValidator {
        checkHash()
        checkSignature()
        checkSenderAddress()
        checkNegativeFee()
        checkNegativeAmount()
        when (type) {
            DEPLOY -> {
                checkEqualFee()
                checkByteCode()
            }
            EXECUTE -> {
                checkEqualFee()
                checkContractCost()
                checkContractMethods()
            }
            FUND -> {
                checkEqualAmount()
            }
        }
        return this
    }

    fun checkNew(type: TransferTransactionType): TransferTransactionPipelineValidator {
        check(type)
        checkActualBalance()
        return this
    }

    fun checkNegativeFee(): TransferTransactionPipelineValidator {
        handlers.add {
            if (it.fee < 0) {
                throw ValidationException("Fee should not be less than 0")
            }
        }
        return this
    }

    fun checkNegativeAmount(): TransferTransactionPipelineValidator {
        handlers.add {
            it as TransferTransaction
            if (it.getPayload().amount < 0) {
                throw ValidationException("Amount should not be less than 0")
            }
        }
        return this
    }

    fun checkEqualFee(): TransferTransactionPipelineValidator {
        handlers.add {
            it as TransferTransaction
            if (it.getPayload().amount == 0L) {
                throw ValidationException("Amount should not be equal to 0")
            }
        }
        return this
    }

    fun checkEqualAmount(): TransferTransactionPipelineValidator {
        handlers.add {
            it as TransferTransaction
            if (it.getPayload().amount == 0L) {
                throw ValidationException("Amount should not be equal to 0")
            }
        }
        return this
    }

    fun checkByteCode(): TransferTransactionPipelineValidator {
        handlers.add {
            it as TransferTransaction
            if (!SmartContractValidator.validate(ByteUtils.fromHexString(it.getPayload().data!!))) {
                throw ValidationException("Invalid smart contract code", INVALID_CONTRACT)
            }
        }
        return this
    }

    fun checkContractCost(): TransferTransactionPipelineValidator {
        handlers.add {
            it as TransferTransaction
            val contract = contractService.getByAddress(it.getPayload().recipientAddress!!)
            if (contract.cost > it.fee) {
                throw ValidationException("Insufficient funds for smart contract execution")
            }
        }
        return this
    }

    fun checkContractMethods(): TransferTransactionPipelineValidator {
        handlers.add { tx ->
            tx as TransferTransaction
            val contract = contractService.getByAddress(tx.getPayload().recipientAddress!!)
            val methods = Abi.fromJson(contract.abi).abiMethods.map { it.name }
            if (!methods.contains(tx.getPayload().data)) {
                throw ValidationException("Smart contract's method ${tx.getPayload().data} not exists",
                    CONTRACT_METHOD_NOT_EXISTS)
            }
        }
        return this
    }

}