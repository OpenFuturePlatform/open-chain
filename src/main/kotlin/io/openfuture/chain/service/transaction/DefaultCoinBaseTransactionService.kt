package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.transaction.CoinBaseTransaction
import io.openfuture.chain.repository.CoinBaseTransactionRepository
import io.openfuture.chain.service.CoinBaseTransactionService
import io.openfuture.chain.service.DelegateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultCoinBaseTransactionService(
    repository: CoinBaseTransactionRepository,
    private val nodeClock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val delegateService: DelegateService
) : DefaultBaseTransactionService<CoinBaseTransaction>(repository), CoinBaseTransactionService {

    companion object {
        private const val COINBASE_ADDRESS = "0x0000000000000000000000000000000000000000"
    }


    @Transactional
    override fun save(tx: CoinBaseTransaction): CoinBaseTransaction = repository.save(tx)

    override fun create(): CoinBaseTransaction {
        val publicKey = HashUtils.toHexString(keyHolder.getPublicKey())
        val delegate = delegateService.getByPublicKey(publicKey)

        return CoinBaseTransaction(nodeClock.networkTime(), 10.0, 0.0, "delegateAddress",
            publicKey, COINBASE_ADDRESS, "signature", "hash")
    }

}