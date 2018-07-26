package io.openfuture.chain.network.domain

import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
class FindAddresses : Packet() {

    override fun toString() = "FindAddresses()"

}