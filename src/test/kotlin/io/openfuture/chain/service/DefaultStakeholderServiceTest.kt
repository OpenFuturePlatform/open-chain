package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.entity.Stakeholder
import io.openfuture.chain.repository.StakeholderRepository
import io.openfuture.chain.service.stakeholder.DefaultStakeholderService
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock


class DefaultStakeholderServiceTest : ServiceTests() {

    private lateinit var serviceBase: StakeholderService

    @Mock private lateinit var repository: StakeholderRepository


    @Before
    fun setUp() {
        serviceBase = DefaultStakeholderService(repository)
    }

    @Test
    fun getAll() {
        val stakeholders = listOf(createStakeholder("publicKey1"), createStakeholder("publicKey2"))

        given(repository.findAll()).willReturn(stakeholders)

        val actualStakeholders = serviceBase.getAll()

        assertThat(actualStakeholders).isEqualTo(stakeholders)
    }

    @Test
    fun getByPublicKey() {
        val delegate = createStakeholder("publicKey")

        given(repository.findOneByPublicKey(delegate.publicKey)).willReturn(delegate)

        val actualDelegate = serviceBase.getByPublicKey(delegate.publicKey)

        assertThat(actualDelegate).isEqualTo(delegate)
    }

    @Test
    fun add() {
        val stakeholderDto = StakeholderDto("address", "publicKey")
        val stakeholder = Stakeholder.of(stakeholderDto)

        given(repository.save(any(Stakeholder::class.java))).will { invocation -> invocation.arguments[0] }

        val actualDelegate = serviceBase.add(stakeholderDto)
        assertThat(actualDelegate.address).isEqualTo(stakeholder.address)
        assertThat(actualDelegate.publicKey).isEqualTo(stakeholder.publicKey)
    }

    private fun createStakeholder(publicKey: String): Stakeholder = Stakeholder("address", publicKey)

}
