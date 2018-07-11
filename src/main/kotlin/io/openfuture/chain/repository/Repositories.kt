package io.openfuture.chain.repository

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.SeedWord
import io.openfuture.chain.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<Block> {

    fun findFirstByOrderByHeightDesc(): Block?

    fun findFirstByVersion(version: Int): Block?

}

@Repository
interface TransactionRepository : BaseRepository<Transaction> {

    fun findAllByBlockIdIsNull(): List<Transaction>

}

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): SeedWord?

}