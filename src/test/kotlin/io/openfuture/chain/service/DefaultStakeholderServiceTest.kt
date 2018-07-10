package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.domain.delegate.StakeholderDto
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.property.DelegateProperties
import io.openfuture.chain.repository.StakeholderRepository
import io.openfuture.chain.repository.DelegateRepository
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock


class DefaultStakeholderServiceTest : ServiceTests() {

    private lateinit var service: StakeholderService

    @Mock private lateinit var repository: StakeholderRepository<Stakeholder>
    @Mock private lateinit var delegateRepository: DelegateRepository
    @Mock private lateinit var delegateProperties: DelegateProperties


    @Before
    fun setUp() {
        service = DefaultStakeholderService(repository, delegateRepository, delegateProperties)
    }

    @Test
    fun getAll() {
        val stakeholders = listOf(createStakeholder("publicKey1"), createStakeholder("publicKey2"))

        given(repository.findAll()).willReturn(stakeholders)

        val actualStakeholders = service.getAllStakeholders()

        assertThat(actualStakeholders).isEqualTo(stakeholders)
    }

    @Test
    fun getByPublicKey() {
        val delegate = createStakeholder("publicKey")

        given(repository.findOneByPublicKey(delegate.publicKey)).willReturn(delegate)

        val actualDelegate = service.getStakeholderByPublicKey(delegate.publicKey)

        assertThat(actualDelegate).isEqualTo(delegate)
    }

    @Test
    fun add() {
        val stakeholderDto = StakeholderDto("username", "address", "publicKey")
        val stakeholder = Stakeholder.of(stakeholderDto)

        given(repository.save(any(Stakeholder::class.java))).will { invocation -> invocation.arguments[0] }

        val actualDelegate = service.addStakeholder(stakeholderDto)

        assertThat(actualDelegate.username).isEqualTo(stakeholder.username)
        assertThat(actualDelegate.address).isEqualTo(stakeholder.address)
        assertThat(actualDelegate.publicKey).isEqualTo(stakeholder.publicKey)
    }

    private fun createStakeholder(publicKey: String): Stakeholder = Stakeholder("username", "address", publicKey)

}
