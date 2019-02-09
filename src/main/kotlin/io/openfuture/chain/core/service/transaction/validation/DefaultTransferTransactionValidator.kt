package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.CONTRACT_METHOD_NOT_EXISTS
import io.openfuture.chain.core.exception.model.ExceptionType.INVALID_CONTRACT
import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.TransferTransactionValidator
import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import io.openfuture.chain.smartcontract.model.Abi
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransferTransactionValidator(
    private val contractService: ContractService
) : TransferTransactionValidator {

    override fun validateNew(utx: UnconfirmedTransferTransaction) {}

    override fun validate(utx: UnconfirmedTransferTransaction) {
        checkNegativeFee(utx)
        checkNegativeAmount(utx)

        when (TransferTransactionType.getType(utx.getPayload().recipientAddress, utx.getPayload().data)) {
            DEPLOY -> {
                checkEqualFee(utx)
                checkByteCode(utx)
            }
            EXECUTE -> {
                checkEqualFee(utx)
                val contract = contractService.getByAddress(utx.getPayload().recipientAddress!!)
                checkContractCost(utx, contract)
                checkContractMethods(utx, contract)
            }
            FUND -> {
                checkEqualAmount(utx)
            }
        }
    }

    private fun checkNegativeFee(utx: UnconfirmedTransferTransaction) {
        if (utx.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }
    }

    private fun checkNegativeAmount(utx: UnconfirmedTransferTransaction) {
        if (utx.getPayload().amount < 0) {
            throw ValidationException("Amount should not be less than 0")
        }
    }

    private fun checkEqualFee(utx: UnconfirmedTransferTransaction) {
        if (utx.fee == 0L) {
            throw ValidationException("Fee should not be equal to 0")
        }
    }

    private fun checkEqualAmount(utx: UnconfirmedTransferTransaction) {
        if (utx.getPayload().amount == 0L) {
            throw ValidationException("Amount should not be equal to 0")
        }
    }

    private fun checkByteCode(utx: UnconfirmedTransferTransaction) {
        if (!SmartContractValidator.validate(ByteUtils.fromHexString(utx.getPayload().data!!))) {
            throw ValidationException("Invalid smart contract code", INVALID_CONTRACT)
        }
    }

    private fun checkContractCost(utx: UnconfirmedTransferTransaction, contract: Contract) {
        if (contract.cost > utx.fee) {
            throw ValidationException("Insufficient funds for smart contract execution")
        }
    }

    private fun checkContractMethods(utx: UnconfirmedTransferTransaction, contract: Contract) {
        val methods = Abi.fromJson(contract.abi).abiMethods.map { it.name }
        if (!methods.contains(utx.getPayload().data)) {
            throw ValidationException("Smart contract's method ${utx.getPayload().data} not exists",
                CONTRACT_METHOD_NOT_EXISTS)
        }
    }

}