package io.openfuture.chain.rpc.domain.base

import io.openfuture.chain.rpc.validation.annotation.SortConstraint
import org.springframework.data.domain.AbstractPageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@SortConstraint
open class PageRequest(
    @field:Min(value = 0) private var offset: Long = 0,
    @field:Min(value = 1) @field:Max(100) private var limit: Int = 100,
    var sortBy: Set<String> = setOf("id"),
    var sortDirection: Direction = Direction.ASC,
    val maySortBy: Map<String, String> = mapOf("id" to "id")
) : AbstractPageRequest(offset.toInt() / limit + 1, limit) {

    override fun next(): Pageable = PageRequest(offset + limit, limit, sortBy, sortDirection)

    override fun getSort(): Sort = if (sortBy.isEmpty()) Sort.unsorted() else Sort.by(sortDirection, *sortBy.toTypedArray())

    override fun first(): Pageable = PageRequest(0, limit, sortBy, sortDirection)

    override fun previous(): PageRequest = if (offset == 0L) this else {
        var newOffset = this.offset - limit
        if (newOffset < 0) newOffset = 0
        PageRequest(newOffset, limit)
    }

    override fun getOffset(): Long = offset

    fun setOffset(offset: Long) {
        this.offset = offset
    }

    fun getLimit(): Int = limit

    fun setLimit(limit: Int) {
        this.limit = limit
    }

    fun toEntityRequest(): PageRequest {
        this.sortBy = sortBy.asSequence().map { maySortBy[it]!! }.toSet()
        return this
    }

}