package io.openfuture.chain.smartcontract.core.model

open class Address(private val address: String) {

    fun getBalance(): Long {
        TODO()
    }

    override fun toString(): String = address

}