package io.openfuture.chain.crypto.repository

import io.openfuture.chain.core.repository.BaseRepository
import io.openfuture.chain.crypto.model.entity.SeedWord
import org.springframework.stereotype.Repository

@Repository
interface SeedWordRepository : BaseRepository<SeedWord> {

    fun findOneByIndex(index: Int): SeedWord

    fun findOneByValue(value: String): SeedWord?

}