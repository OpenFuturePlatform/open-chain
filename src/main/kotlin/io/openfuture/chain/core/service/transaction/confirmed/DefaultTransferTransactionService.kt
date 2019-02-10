package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.*
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.UTransferTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.smartcontract.component.ByteCodeProcessor
import io.openfuture.chain.smartcontract.component.SmartContractInjector
import io.openfuture.chain.smartcontract.component.abi.AbiGenerator
import io.openfuture.chain.smartcontract.component.load.SmartContractLoader
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import io.openfuture.chain.smartcontract.execution.ContractExecutor
import io.openfuture.chain.smartcontract.util.SerializationUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransferTransactionService(
    private val repository: TransferTransactionRepository,
    private val uTransferTransactionService: UTransferTransactionService,
    private val contractService: ContractService,
    private val contractCostCalculator: ContractCostCalculator,
    private val contractExecutor: ContractExecutor
) : DefaultExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction, TransferTransactionRepository,
    UTransferTransactionService>(repository, uTransferTransactionService), TransferTransactionService {

    override fun getByAddress(address: String, request: PageRequest): Page<TransferTransaction> =
        repository.findAllBySenderAddressOrPayloadRecipientAddress(address, address, request.toEntityRequest())

    @Transactional
    override fun commit(tx: TransferTransaction, receipt: Receipt): TransferTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(tx.hash)
            if (null != persistTx) {
                return persistTx
            }

            if (DEPLOY == TransferTransactionType.getType(tx.getPayload().recipientAddress, tx.getPayload().data)
                && receipt.getResults().all { it.error == null }) {
                val bytecode = ByteUtils.fromHexString(tx.getPayload().data!!)
                val address = contractService.generateAddress(tx.senderAddress)
                val abi = AbiGenerator.generate(bytecode)
                val cost = contractCostCalculator.calculateCost(bytecode)
                val newBytes = ByteUtils.toHexString(ByteCodeProcessor.renameClass(bytecode, address))
                contractService.save(Contract(address, tx.senderAddress, newBytes, abi, cost))
            }

            val utx = uTransferTransactionService.findByHash(tx.hash)
            if (null != utx) {
                return confirm(utx, tx)
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(uTx: UnconfirmedTransferTransaction, delegateWallet: String): Receipt {
        val results = mutableListOf<ReceiptResult>()

        when (TransferTransactionType.getType(uTx.getPayload().recipientAddress, uTx.getPayload().data)) {
            FUND -> {
                stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -(uTx.getPayload().amount + uTx.fee))
                stateManager.updateWalletBalanceByAddress(uTx.getPayload().recipientAddress!!, uTx.getPayload().amount)
                results.add(ReceiptResult(uTx.senderAddress, uTx.getPayload().recipientAddress!!, uTx.getPayload().amount))
                results.add(ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee))
            }
            DEPLOY -> {
                val bytecode = ByteUtils.fromHexString(uTx.getPayload().data)
                val contractCost = contractCostCalculator.calculateCost(bytecode)

                if (uTx.fee >= contractCost) {
                    val contractAddress = contractService.generateAddress(uTx.senderAddress)
                    val newBytes = ByteCodeProcessor.renameClass(bytecode, contractAddress)
                    val clazz = SmartContractLoader(this::class.java.classLoader).loadClass(newBytes)
                    val contract = SmartContractInjector.initSmartContract(clazz, uTx.senderAddress, contractAddress)
                    stateManager.updateSmartContractStorage(contractAddress, ByteUtils.toHexString(SerializationUtils.serialize(contract)))
                    stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -contractCost)
                    stateManager.updateWalletBalanceByAddress(delegateWallet, contractCost)
                    results.add(ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee))

                    val delivery = uTx.fee - contractCost
                    if (0 < delivery) {
                        results.add(ReceiptResult(delegateWallet, uTx.senderAddress, delivery))
                    }
                } else {
                    stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -uTx.fee)
                    stateManager.updateWalletBalanceByAddress(delegateWallet, uTx.fee)
                    results.add(ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee,
                        "The fee was charged, but this is not enough.", "Contract is not deployed.")
                    )
                }
            }
            EXECUTE -> {
                val contractState = stateManager.getLastByAddress<AccountState>(uTx.getPayload().recipientAddress!!)
                val result = contractExecutor.run(contractState.storage!!, uTx, delegateWallet)
                result.receipt.forEach {
                    stateManager.updateWalletBalanceByAddress(it.from, -it.amount)
                    stateManager.updateWalletBalanceByAddress(it.to, it.amount)
                }
                results.addAll(result.receipt)
                result.state?.let {
                    stateManager.updateSmartContractStorage(uTx.getPayload().recipientAddress!!, it)
                }
            }
        }

        return getReceipt(uTx.hash, results)
    }

}