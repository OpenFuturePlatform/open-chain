package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.UDelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultUDelegateTransactionService(
    uRepository: UDelegateTransactionRepository,
    private val consensusProperties: ConsensusProperties
) : DefaultUTransactionService<UnconfirmedDelegateTransaction, UDelegateTransactionRepository>(uRepository),
    UDelegateTransactionService {

    override fun process(uTx: UnconfirmedDelegateTransaction, delegateWallet: String): Receipt {
        stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -(uTx.getPayload().amount + uTx.fee))
        stateManager.updateWalletBalanceByAddress(consensusProperties.genesisAddress!!, uTx.getPayload().amount)
        stateManager.addDelegate(uTx.getPayload().delegateKey, uTx.senderAddress, uTx.timestamp)

        return generateReceipt(uTx, delegateWallet)
    }

    private fun generateReceipt(uTx: UnconfirmedDelegateTransaction, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(uTx.senderAddress, consensusProperties.genesisAddress!!, uTx.getPayload().amount,
                uTx.getPayload().delegateKey),
            ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee)
        )
        return getReceipt(uTx.hash, results)
    }

}