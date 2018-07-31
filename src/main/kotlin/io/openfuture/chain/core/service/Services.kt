package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.UTransaction

interface CommonBlockService {

    fun get(hash: String): Block

    fun getLast(): Block

    fun getBlocksAfterCurrentHash(hash: String): List<Block>?

    fun isExists(hash: String): Boolean

    fun isValid(block: Block): Boolean

}

interface CommonTransactionService {

    fun get(hash: String): Transaction

    fun isExists(hash: String): Boolean

}

interface UCommonTransactionService {

    fun getAll(): MutableSet<UTransaction>

}
