package io.openfuture.chain.rpc.domain.base

import io.openfuture.chain.rpc.validation.annotation.SortConstraint
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
) : Pageable {

    override fun getPageNumber(): Int = offset.toInt() / limit + 1

    override fun hasPrevious(): Boolean = offset > 0

    override fun getPageSize(): Int = limit

    override fun previousOrFirst(): Pageable = if (hasPrevious()) previous() else first()

    override fun next(): Pageable = PageRequest(offset + limit, limit, sortBy, sortDirection, maySortBy)

    override fun getSort(): Sort = if (sortBy.isEmpty()) Sort.unsorted() else Sort.by(sortDirection, *sortBy.toTypedArray())

    override fun first(): Pageable = PageRequest(0, limit, sortBy, sortDirection, maySortBy)

    override fun getOffset(): Long = offset

    fun setOffset(offset: Long) {
        this.offset = offset
    }

    fun getLimit(): Int = limit

    fun setLimit(limit: Int) {
        this.limit = limit
    }

    fun previous(): PageRequest = if (offset == 0L) this else {
        var newOffset = this.offset - limit
        if (newOffset < 0) newOffset = 0
        PageRequest(newOffset, limit, sortBy, sortDirection, maySortBy)
    }

    fun toEntityRequest(): PageRequest {
        this.sortBy = sortBy.asSequence().map { maySortBy[it]!! }.toSet()
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PageRequest) return false

        if (offset != other.offset) return false
        if (limit != other.limit) return false
        if (sortBy != other.sortBy) return false
        if (sortDirection != other.sortDirection) return false
        if (maySortBy != other.maySortBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = offset.hashCode()
        result = 31 * result + limit
        result = 31 * result + sortBy.hashCode()
        result = 31 * result + sortDirection.hashCode()
        result = 31 * result + maySortBy.hashCode()
        return result
    }


}