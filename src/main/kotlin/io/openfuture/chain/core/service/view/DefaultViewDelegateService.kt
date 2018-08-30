package io.openfuture.chain.core.service.view

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
import io.openfuture.chain.core.repository.ViewDelegateRepository
import io.openfuture.chain.core.service.ViewDelegateService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class DefaultViewDelegateService(
    private val repository: ViewDelegateRepository
) : ViewDelegateService {

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<ViewDelegate> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getByNodeId(nodeId: String): ViewDelegate = repository.findOneByNodeId(nodeId)
        ?: throw NotFoundException("Delegate view with nodeId: $nodeId not exist!")

}