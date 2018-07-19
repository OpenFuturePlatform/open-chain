package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.transaction.CoinBaseTransaction
import io.openfuture.chain.property.ConsensusProperties
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
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties
) : DefaultBaseTransactionService<CoinBaseTransaction>(repository), CoinBaseTransactionService {

    @Transactional
    override fun save(tx: CoinBaseTransaction): CoinBaseTransaction = repository.save(tx)

    override fun create(fees: Double): CoinBaseTransaction {
        val publicKey = HashUtils.toHexString(keyHolder.getPublicKey())
        val delegate = delegateService.getByPublicKey(publicKey)

        return CoinBaseTransaction(nodeClock.networkTime(), fees + 10.0, 0.0, delegate.getAddress(),
            publicKey, consensusProperties.genesisAddress!!).sign(keyHolder.getPrivateKey())
    }

}