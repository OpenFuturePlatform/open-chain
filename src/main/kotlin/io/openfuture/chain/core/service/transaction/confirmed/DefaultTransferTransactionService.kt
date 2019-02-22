package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.core.util.SerializationUtils
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.smartcontract.component.ByteCodeProcessor
import io.openfuture.chain.smartcontract.component.SmartContractInjector
import io.openfuture.chain.smartcontract.component.abi.AbiGenerator
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import io.openfuture.chain.smartcontract.execution.ContractExecutor
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransferTransactionService(
    private val repository: TransferTransactionRepository,
    private val contractService: ContractService,
    private val contractCostCalculator: ContractCostCalculator,
    private val contractExecutor: ContractExecutor
) : DefaultExternalTransactionService<TransferTransaction>(repository), TransferTransactionService {

    override fun getAllByAddress(address: String, request: PageRequest): Page<TransferTransaction> =
        repository.findAllBySenderAddressOrPayloadRecipientAddress(address, address, request.toEntityRequest())

    @Transactional
    override fun commit(tx: TransferTransaction, receipt: Receipt): TransferTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(tx.hash)
            if (null != persistTx) {
                return persistTx
            }

            if (DEPLOY == tx.getType() && receipt.isSuccessful()) {
                val bytecode = ByteUtils.fromHexString(tx.getPayload().data!!)
                val address = contractService.generateAddress(tx.senderAddress)
                val abi = AbiGenerator.generate(bytecode)
                val cost = contractCostCalculator.calculateCost(bytecode)
                val newBytes = ByteUtils.toHexString(ByteCodeProcessor.renameClass(bytecode, address))
                contractService.save(Contract(address, tx.senderAddress, newBytes, abi, cost))
            }

            val utx = uRepository.findOneByHash(tx.hash)
            if (null != utx) {
                return confirm(utx, tx)
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(tx: TransferTransaction, delegateWallet: String): Receipt {
        val results = mutableListOf<ReceiptResult>()

        when (tx.getType()) {
            FUND -> {
                stateManager.updateWalletBalanceByAddress(tx.senderAddress, -(tx.getPayload().amount + tx.fee))
                stateManager.updateWalletBalanceByAddress(tx.getPayload().recipientAddress!!, tx.getPayload().amount)
                stateManager.updateWalletBalanceByAddress(delegateWallet, tx.fee)
                results.add(ReceiptResult(tx.senderAddress, tx.getPayload().recipientAddress!!, tx.getPayload().amount))
                results.add(ReceiptResult(tx.senderAddress, delegateWallet, tx.fee))
            }
            DEPLOY -> {
                val bytecode = ByteUtils.fromHexString(tx.getPayload().data)
                val contractCost = contractCostCalculator.calculateCost(bytecode)

                if (tx.fee >= contractCost) {
                    val contractAddress = contractService.generateAddress(tx.senderAddress)
                    val newBytes = ByteCodeProcessor.renameClass(bytecode, contractAddress)
                    val clazz = SmartContractLoader(this::class.java.classLoader).loadClass(newBytes)
                    val contract = SmartContractInjector.initSmartContract(clazz, tx.senderAddress, contractAddress)
                    stateManager.updateSmartContractStorage(contractAddress,
                        ByteUtils.toHexString(SerializationUtils.serialize(contract)))
                    stateManager.updateWalletBalanceByAddress(tx.senderAddress, -contractCost)
                    stateManager.updateWalletBalanceByAddress(delegateWallet, contractCost)
                    results.add(ReceiptResult(tx.senderAddress, delegateWallet, tx.fee, contractAddress))

                    val delivery = tx.fee - contractCost
                    if (0 < delivery) {
                        results.add(ReceiptResult(delegateWallet, tx.senderAddress, delivery))
                    }
                } else {
                    stateManager.updateWalletBalanceByAddress(tx.senderAddress, -tx.fee)
                    stateManager.updateWalletBalanceByAddress(delegateWallet, tx.fee)
                    results.add(ReceiptResult(tx.senderAddress, delegateWallet, tx.fee,
                        error = "Contract is not deployed. The fee was charged, but this is not enough for deploy.")
                    )
                }
            }
            EXECUTE -> {
                val contractState = stateManager.getByAddress<AccountState>(tx.getPayload().recipientAddress!!)
                val result = contractExecutor.run(contractState.storage!!, tx, delegateWallet)
                result.receipt.forEach {
                    stateManager.updateWalletBalanceByAddress(it.from, -it.amount)
                    stateManager.updateWalletBalanceByAddress(it.to, it.amount)
                }
                results.addAll(result.receipt)
                result.state?.let {
                    stateManager.updateSmartContractStorage(tx.getPayload().recipientAddress!!, it)
                }
            }
        }

        return getReceipt(tx.hash, results)
    }

}