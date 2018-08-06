package io.openfuture.chain.core.repository

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Int>, PagingAndSortingRepository<T, Int>

@Repository
interface BlockRepository<Entity: BaseBlock> : BaseRepository<Entity>{

    fun findOneByHash(hash: String): Entity?

    fun findFirstByOrderByHeightDesc(): Entity?

}

@Repository
interface TransactionRepository<Entity : Transaction> : BaseRepository<Entity> {

    fun findOneByHash(hash: String): Entity?

}

@Repository
interface UTransactionRepository<UEntity : UnconfirmedTransaction> : BaseRepository<UEntity> {

    fun findOneByHash(hash: String): UEntity?

    fun findAllByOrderByFeeDesc(): MutableList<UEntity>

}

@Repository
interface DelegateRepository : BaseRepository<Delegate> {

    fun findOneByPublicKey(key: String): Delegate?

}

@Repository
interface WalletRepository : BaseRepository<Wallet> {

    fun findOneByAddress(address: String): Wallet?

}