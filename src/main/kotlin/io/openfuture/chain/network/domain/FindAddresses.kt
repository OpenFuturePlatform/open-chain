package io.openfuture.chain.network.domain

import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
class FindAddresses : Packet() {

    override fun toString() = "FindAddresses()"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}