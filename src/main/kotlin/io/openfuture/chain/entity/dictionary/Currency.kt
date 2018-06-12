package io.openfuture.chain.entity.dictionary

import io.openfuture.chain.entity.base.Dictionary

/**
 * @author Homza Pavel
 */
enum class Currency(
        private val id: Int,
        private val value: String
) : Dictionary {

    USD(1, "usd"),
    GBP(2, "gpb"),
    EUR(3, "eur"),
    CNY(4, "cny"),
    ETH(5, "eth")
    ;

    override fun getId(): Int = id

    fun getValue(): String = value

}