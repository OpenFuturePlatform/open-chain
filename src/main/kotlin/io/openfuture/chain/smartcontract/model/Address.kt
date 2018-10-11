package io.openfuture.chain.smartcontract.model

open class Address(private val address: String) {

    fun getBalance(): Long {
        TODO()
    }

    override fun toString(): String = address

}