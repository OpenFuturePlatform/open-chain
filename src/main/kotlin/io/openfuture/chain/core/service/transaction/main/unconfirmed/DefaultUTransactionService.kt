package io.openfuture.chain.core.service.transaction.main.unconfirmed

import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.core.model.dto.transaction.data.BaseTransactionData
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.UCommonTransactionService
import io.openfuture.chain.core.service.UTransactionService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.rpc.domain.transaction.BaseTransactionRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultUTransactionService<UEntity : UTransaction, Data : BaseTransactionData,
    Dto: BaseTransactionDto<Data>, Req: BaseTransactionRequest<UEntity, Data>>(
    protected open var repository: UTransactionRepository<UEntity>
) : UTransactionService<UEntity, Data, Dto, Req> {

    @Autowired
    protected lateinit var nodeClock: NodeClock

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var consensusProperties: ConsensusProperties

    @Autowired
    private lateinit var serviceCommon: UCommonTransactionService

    @Transactional
    open fun validate(dto: Dto) {
        if (!isValidHash(dto.data, dto.senderSignature, dto.senderPublicKey, dto.hash)) {
            throw ValidationException("Invalid transaction hash")
        }
        commonValidate(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    @Transactional
    open fun validate(request: Req) {
        commonValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

    protected fun saveAndBroadcast(tx: UEntity): UEntity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

    protected fun commonValidate(data: Data, signature: String, publicKey: String) {
        //todo need to add address validation

        if (!isValidSenderBalance(data.senderAddress, data.amount)) {
            throw ValidationException("Invalid wallet balance")
        }

        if (!isValidaSignature(data, signature, publicKey)) {
            throw ValidationException("Invalid transaction signature")
        }
    }

    private fun isValidHash(data: Data, publicKey: String, signature: String, hash: String): Boolean {
        return TransactionUtils.createHash(data, publicKey, signature) == hash
    }

    private fun isValidaSignature(data: Data, publicKey: String, signature: String): Boolean {
        return SignatureUtils.verify(data.getBytes(), signature, ByteUtils.fromHexString(publicKey))
    }

    private fun isValidSenderBalance(senderAddress: String, amount: Long): Boolean {
        if (consensusProperties.genesisAddress!! == senderAddress) {
            return true
        }

        val balance = walletService.getBalanceByAddress(senderAddress)
        val unconfirmedOutput = serviceCommon.getAll()
            .filter { it.senderAddress == senderAddress }
            .map { it.amount + it.fee }
            .sum()

        val unspentBalance = balance - unconfirmedOutput
        if (unspentBalance < amount) {
            return false
        }
        return true
    }

}