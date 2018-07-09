package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.domain.delegate.AccountDto
import io.openfuture.chain.entity.Account
import io.openfuture.chain.property.DelegateProperties
import io.openfuture.chain.repository.AccountRepository
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock


class DefaultAccountServiceTest : ServiceTests() {

    private lateinit var service: AccountService

    @Mock private lateinit var repository: AccountRepository
    @Mock private lateinit var delegateProperties: DelegateProperties


    @Before
    fun setUp() {
        service = DefaultAccountService(repository, delegateProperties)
    }

    @Test
    fun getAll() {
        val delegates = listOf(createDelegate("publicKey1"), createDelegate("publicKey2"))

        given(repository.findAll()).willReturn(delegates)

        val actualDelegates = service.getAll()

        assertThat(actualDelegates).isEqualTo(delegates)
    }

    @Test
    fun getByPublicKey() {
        val delegate = createDelegate("publicKey")

        given(repository.findOneByPublicKey(delegate.publicKey)).willReturn(delegate)

        val actualDelegate = service.getAccountByPublicKey(delegate.publicKey)

        assertThat(actualDelegate).isEqualTo(delegate)
    }

    @Test
    fun add() {
        val delegateDto = AccountDto("username", "address", "publicKey")
        val delegate = Account.of(delegateDto)

        given(repository.save(any(Account::class.java))).will { invocation -> invocation.arguments[0] }

        val actualDelegate = service.add(delegateDto)

        assertThat(actualDelegate.username).isEqualTo(delegate.username)
        assertThat(actualDelegate.address).isEqualTo(delegate.address)
        assertThat(actualDelegate.publicKey).isEqualTo(delegate.publicKey)
    }

    private fun createDelegate(publicKey: String): Account = Account("username", "address", publicKey)

}
