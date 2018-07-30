package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.UTransaction
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>, PagingAndSortingRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<Block>{

    fun findByHash(hash: String): Block?

    fun findByHeightGreaterThan(height: Long): List<Block>?

    fun findFirstByOrderByHeightDesc(): Block?

    fun existsByHash(hash: String): Boolean

}

@Repository
interface TransactionRepository<Entity : Transaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

}

@Repository
interface UTransactionRepository<UEntity : UTransaction> : BaseRepository<UEntity> {

    fun findOneByHash(hash: String): UEntity?

    fun findAllByOrderByFeeDesc(pageable: Pageable): MutableList<UEntity>

}