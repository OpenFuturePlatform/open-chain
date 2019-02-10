package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.model.entity.Contract
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType.DEPLOY
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.service.ContractService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.smartcontract.component.ByteCodeProcessor
import io.openfuture.chain.smartcontract.component.abi.AbiGenerator
import io.openfuture.chain.smartcontract.deploy.calculation.ContractCostCalculator
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransferTransactionService(
    private val repository: TransferTransactionRepository,
    private val contractService: ContractService,
    private val contractCostCalculator: ContractCostCalculator
) : DefaultExternalTransactionService<TransferTransaction, TransferTransactionRepository>(repository), TransferTransactionService {

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

            if (DEPLOY == TransferTransactionType.getType(tx.getPayload().recipientAddress, tx.getPayload().data)
                && receipt.getResults().all { it.error == null }) {
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

}