package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.CONTRACT_METHOD_NOT_EXISTS
import io.openfuture.chain.core.exception.model.ExceptionType.INVALID_CONTRACT
import io.openfuture.chain.core.model.entity.Contract
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
class TransferTransactionValidator(
    private val contractService: ContractService
) : TransactionValidator() {

    fun check(): Array<TransactionValidateHandler> = arrayOf(
        checkHash(),
        checkSignature(),
        checkSenderAddress(),
        checkNegativeFee(),
        checkNegativeAmount(),
        checkByType()
    )

    fun checkNew(unconfirmedBalance: Long): Array<TransactionValidateHandler> = arrayOf(
        *check(),
        checkActualBalance(unconfirmedBalance)
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

    fun checkByType(): TransactionValidateHandler = { tx ->
        tx as TransferTransaction
        when (tx.getType()) {
            DEPLOY -> {
                checkEqualFee(tx)
                checkByteCode(tx)
            }
            EXECUTE -> {
                checkEqualFee(tx)
                val contract = contractService.getByAddress(tx.getPayload().recipientAddress!!)
                checkContractCost(tx, contract)
                checkContractMethods(tx, contract)
            }
            FUND -> {
                checkEqualAmount(tx)
            }
        }
    }

    private fun checkEqualFee(tx: TransferTransaction) {
        if (tx.fee == 0L) {
            throw ValidationException("Fee should not be equal to 0")
        }
    }

    private fun checkEqualAmount(tx: TransferTransaction) {
        if (tx.getPayload().amount == 0L) {
            throw ValidationException("Amount should not be equal to 0")
        }
    }

    private fun checkByteCode(tx: TransferTransaction) {
        if (!SmartContractValidator.validate(ByteUtils.fromHexString(tx.getPayload().data!!))) {
            throw ValidationException("Invalid smart contract code", INVALID_CONTRACT)
        }
    }

    private fun checkContractCost(tx: TransferTransaction, contract: Contract) {
        if (contract.cost > tx.fee) {
            throw ValidationException("Insufficient funds for smart contract execution")
        }
    }

    private fun checkContractMethods(tx: TransferTransaction, contract: Contract) {
        val methods = Abi.fromJson(contract.abi).abiMethods.map { it.name }
        if (!methods.contains(tx.getPayload().data)) {
            throw ValidationException("Smart contract's method ${tx.getPayload().data} not exists",
                CONTRACT_METHOD_NOT_EXISTS)
        }
    }

}