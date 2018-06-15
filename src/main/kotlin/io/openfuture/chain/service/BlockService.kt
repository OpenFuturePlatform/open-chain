package io.openfuture.chain.service

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.entity.Block

/**
 * @author Homza Pavel
 */
interface BlockService {

    fun get(id: Int): Block

    fun getAll(): MutableList<Block>

    fun getLast(): Block?

    fun save(request: BlockRequest): Block

}