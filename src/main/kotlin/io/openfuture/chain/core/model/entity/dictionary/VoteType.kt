package io.openfuture.chain.core.model.entity.dictionary

import io.openfuture.chain.core.exception.NotFoundVoteTypeException
import io.openfuture.chain.core.model.entity.base.Dictionary

enum class VoteType(
    private val id: Int
) : Dictionary {

    FOR(1),
    AGAINST(2);

    override fun getId(): Int = id

    companion object {
        fun getById(id: Int): VoteType {
            for (value in values()) {
                if (value.id == id) {
                    return value
                }
            }

            throw NotFoundVoteTypeException("Type with such id: $id is not found!")
        }
    }

}