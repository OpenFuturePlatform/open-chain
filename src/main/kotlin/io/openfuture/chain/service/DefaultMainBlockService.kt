package io.openfuture.chain.service

import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.repository.MainBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    val mainBlockRepository: MainBlockRepository
) : MainBlockService {

    @Transactional(readOnly = true)
    override fun getLast(): MainBlock =
        mainBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Main block not found!")

}