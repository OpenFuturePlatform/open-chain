package io.openfuture.chain.repository

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.SeedWord
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.entity.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>

@Repository
interface BlockRepository : BaseRepository<Block> {

    fun findFirstByOrderByOrderNumberDesc(): Block?

}

@Repository
interface TransactionRepository : BaseRepository<Transaction> {

    fun findByRecipientKey(recipientKey: String): List<Transaction>

    fun findBySenderKey(senderKey: String): List<Transaction>

}

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): Optional<SeedWord>

}

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}
