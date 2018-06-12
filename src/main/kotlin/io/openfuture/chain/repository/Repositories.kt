package io.openfuture.chain.repository

import io.openfuture.chain.entity.Block
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T> : JpaRepository<T, Long>

@Repository
interface BlockRepository : BaseRepository<Block>
