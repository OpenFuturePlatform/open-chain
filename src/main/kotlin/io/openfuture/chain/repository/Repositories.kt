package io.openfuture.chain.repository

import io.openfuture.chain.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository<T: Block> : BaseRepository<T> {

    fun findByHash(hash: String): Block?

    fun findFirstByOrderByHeightDesc(): T?

    fun findFirstByTypeIdOrderByHeight(typeId: Int): T?

}

@Repository
interface MainBlockRepository : BlockRepository<MainBlock>

@Repository
interface GenesisBlockRepository : BlockRepository<GenesisBlock>

@Repository
interface TransactionRepository : BaseRepository<Transaction> {

    fun findAllByBlockHashIsNull(): List<Transaction>

}

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): SeedWord?

}

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}
