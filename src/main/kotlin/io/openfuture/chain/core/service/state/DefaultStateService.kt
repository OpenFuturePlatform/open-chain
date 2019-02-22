package io.openfuture.chain.core.service.state

import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.repository.StateRepository
import io.openfuture.chain.core.service.StateService
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultStateService<T : State>(
    private val repository: StateRepository<T>
) : StateService<T> {

    override fun getAll(): List<T> = repository.findAll()

}