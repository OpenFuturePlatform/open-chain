package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType
import io.openfuture.chain.core.exception.model.ExceptionType.INSUFFICIENT_ACTUAL_BALANCE
import io.openfuture.chain.core.exception.model.ExceptionType.INVALID_CONTRACT
import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.Companion.getType
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import io.openfuture.chain.smartcontract.component.ByteCodeProcessor
import io.openfuture.chain.smartcontract.component.SmartContractInjector
import io.openfuture.chain.smartcontract.component.abi.AbiGenerator
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.component.validation.SmartContractValidator
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import io.openfuture.chain.smartcontract.execution.ContractExecutor
import io.openfuture.chain.smartcontract.model.Abi
import io.openfuture.chain.smartcontract.util.SerializationUtils.serialize
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository,
    private val contractService: ContractService,
    private val contractCostCalculator: ContractCostCalculator,
    private val contractExecutor: ContractExecutor
) : ExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction>(repository, uRepository), TransferTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultTransferTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long = unconfirmedRepository.count()

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): TransferTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(request: TransactionPageRequest): Page<TransferTransaction> =
        repository.findAll(request.toEntityRequest())

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedTransferTransaction> =
        unconfirmedRepository.findAllByOrderByHeaderFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction =
        unconfirmedRepository.findOneByFooterHash(hash)
            ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getByAddress(address: String, request: TransactionPageRequest): Page<TransferTransaction> =
        (repository as TransferTransactionRepository).findAllByHeaderSenderAddressOrPayloadRecipientAddress(address, address, request.toEntityRequest())

    @BlockchainSynchronized
    @Transactional
    override fun add(message: TransferTransactionMessage) {
        BlockchainLock.writeLock.lock()
        try {
            super.add(UnconfirmedTransferTransaction.of(message))
        } catch (ex: CoreException) {
            log.debug(ex.message)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction {
        BlockchainLock.writeLock.lock()
        try {
            return super.add(UnconfirmedTransferTransaction.of(request))
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional
    override fun commit(transaction: TransferTransaction, receipt: Receipt): TransferTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val tx = repository.findOneByFooterHash(transaction.footer.hash)
            if (null != tx) {
                return tx
            }

            if (DEPLOY == getType(transaction.payload.recipientAddress, transaction.payload.data) && receipt.getResults().all { it.error == null }) {
                val bytecode = fromHexString(transaction.payload.data!!)
                val address = contractService.generateAddress(transaction.header.senderAddress)
                val abi = AbiGenerator.generate(bytecode)
                val cost = contractCostCalculator.calculateCost(bytecode)
                val newBytes = ByteUtils.toHexString(ByteCodeProcessor.renameClass(bytecode, address))
                contractService.save(Contract(address, transaction.header.senderAddress, newBytes, abi, cost))
            }

            val utx = unconfirmedRepository.findOneByFooterHash(transaction.footer.hash)
            if (null != utx) {
                return confirm(utx, transaction)
            }

            return save(transaction)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }


    override fun process(message: TransferTransactionMessage, delegateWallet: String): Receipt {
        val results = mutableListOf<ReceiptResult>()

        when (getType(message.recipientAddress, message.data)) {
            FUND -> {
                stateManager.updateWalletBalanceByAddress(message.senderAddress, -(message.amount + message.fee))
                stateManager.updateWalletBalanceByAddress(message.recipientAddress!!, message.amount)
                results.add(ReceiptResult(message.senderAddress, message.recipientAddress!!, message.amount))
                results.add(ReceiptResult(message.senderAddress, delegateWallet, message.fee))
            }
            DEPLOY -> {
                val bytecode = fromHexString(message.data)
                val contractCost = contractCostCalculator.calculateCost(bytecode)

                if (message.fee >= contractCost) {
                    val contractAddress = contractService.generateAddress(message.senderAddress)
                    val newBytes = ByteCodeProcessor.renameClass(bytecode, contractAddress)
                    val clazz = SmartContractLoader(this::class.java.classLoader).loadClass(newBytes)
                    val contract = SmartContractInjector.initSmartContract(clazz, message.senderAddress, contractAddress)
                    stateManager.updateSmartContractStorage(contractAddress, toHexString(serialize(contract)))
                    stateManager.updateWalletBalanceByAddress(message.senderAddress, -contractCost)
                    stateManager.updateWalletBalanceByAddress(delegateWallet, contractCost)
                    results.add(ReceiptResult(message.senderAddress, delegateWallet, message.fee))

                    val delivery = message.fee - contractCost
                    if (0 < delivery) {
                        results.add(ReceiptResult(delegateWallet, message.senderAddress, delivery))
                    }
                } else {
                    stateManager.updateWalletBalanceByAddress(message.senderAddress, -message.fee)
                    stateManager.updateWalletBalanceByAddress(delegateWallet, message.fee)
                    results.add(ReceiptResult(message.senderAddress, delegateWallet, message.fee,
                        "The fee was charged, but this is not enough.", "Contract is not deployed.")
                    )
                }
            }
            EXECUTE -> {
                val contractState = stateManager.getLastByAddress<AccountState>(message.recipientAddress!!)
                val result = contractExecutor.run(contractState.storage!!, message, delegateWallet)
                result.receipt.forEach {
                    stateManager.updateWalletBalanceByAddress(it.from, -it.amount)
                    stateManager.updateWalletBalanceByAddress(it.to, it.amount)
                }
                results.addAll(result.receipt)
                result.state?.let {
                    stateManager.updateSmartContractStorage(message.recipientAddress!!, it)
                }
            }
        }

        return getReceipt(message.hash, results)
    }

    override fun verify(message: TransferTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedTransferTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: TransferTransaction): TransferTransaction = super.save(tx)

    override fun validate(utx: UnconfirmedTransferTransaction) {
        super.validate(utx)

        if (utx.header.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }
        if (utx.payload.amount < 0) {
            throw ValidationException("Amount should not be less than 0")
        }

        when (getType(utx.payload.recipientAddress, utx.payload.data)) {
            DEPLOY -> {

                if (utx.header.fee == 0L) {
                    throw ValidationException("Fee should not be equal to 0")
                }
                if (!SmartContractValidator.validate(fromHexString(utx.payload.data!!))) {
                    throw ValidationException("Invalid smart contract code", INVALID_CONTRACT)
                }
            }
            EXECUTE -> {
                if (utx.header.fee == 0L) {
                    throw ValidationException("Fee should not be equal to 0")
                }

                val contract = contractService.getByAddress(utx.payload.recipientAddress!!)
                val methods = Abi.fromJson(contract.abi).abiMethods.map { it.name }
                if (contract.cost > utx.header.fee) {
                    throw ValidationException("Insufficient funds for smart contract execution")
                }
                if (!methods.contains(utx.payload.data)) {
                    throw ValidationException("Smart contract's method ${utx.payload.data} not exists",
                        ExceptionType.CONTRACT_METHOD_NOT_EXISTS)
                }
            }
            FUND -> {
                if (utx.payload.amount == 0L) {
                    throw ValidationException("Amount should not be equal to 0")
                }
            }
        }

    }

    @Transactional(readOnly = true)
    override fun validateNew(utx: UnconfirmedTransferTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.payload.amount + utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", INSUFFICIENT_ACTUAL_BALANCE)
        }
    }

}