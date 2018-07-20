package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.entity.transaction.CoinBaseTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.CoinBaseTransactionRepository
import io.openfuture.chain.service.CoinBaseTransactionService
import io.openfuture.chain.service.DelegateService
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class DefaultCoinBaseTransactionServiceTest : ServiceTests() {

    @Mock private lateinit var repository: CoinBaseTransactionRepository
    @Mock private lateinit var nodeClock: NodeClock
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var delegateService: DelegateService
    @Mock private lateinit var properties: ConsensusProperties

    private lateinit var service: CoinBaseTransactionService


    @Before
    fun setUp() {
        service = DefaultCoinBaseTransactionService(repository, nodeClock, keyHolder, delegateService, properties)
    }

    @Test
    fun save() {
        val coinBaseTransaction = createCoinBaseTransaction()

        given(repository.save(coinBaseTransaction)).willReturn(coinBaseTransaction)

        val actualCoinBaseTransaction = service.save(coinBaseTransaction)

        assertThat(actualCoinBaseTransaction).isEqualTo(coinBaseTransaction)
    }

    private fun createCoinBaseTransaction(): CoinBaseTransaction = CoinBaseTransaction(
        1500000000L,
        10.0,
        0.0,
        "recipient_address",
        "sender_key",
        "0x00000",
        "hash",
        "sender_signature"
    )

}
