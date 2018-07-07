package io.openfuture.chain.repository

import io.openfuture.chain.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<Block> {

    fun findFirstByOrderByHeightDesc(): Block?

}

@Repository
interface TransactionRepository: BaseRepository<Transaction> {

    fun findOneByHash(hash: String): Transaction?

    fun findAllByBlockIsNull(): MutableSet<Transaction>

}

@Repository
interface VoteTransactionRepository: BaseRepository<VoteTransaction>

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): Optional<SeedWord>

}

@Repository
interface VoteRepository: BaseRepository<Vote> {

    fun findAllByPublicKey(publicKey: String): List<Vote>

}

@Repository
interface GenesisBlockRepository : BaseRepository<GenesisBlock>

