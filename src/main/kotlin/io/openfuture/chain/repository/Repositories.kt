package io.openfuture.chain.repository

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Peer
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

    fun findFirstByOrderByOrderNumberDesc(): Block?

}

@Repository
interface TransactionRepository : BaseRepository<Transaction>

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): SeedWord?

}

@Repository
interface PeerRepository : BaseRepository<Peer> {

    fun deleteOneByNetworkId(networkId: String)

    fun findOneByNetworkId(networkId: String): Optional<Peer>

}
