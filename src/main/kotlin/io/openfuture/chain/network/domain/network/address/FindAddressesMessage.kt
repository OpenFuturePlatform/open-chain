package io.openfuture.chain.network.domain.network.address

import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage

@NoArgConstructor
class FindAddressesMessage : BaseMessage {

    override fun toString() = "FindAddressesMessage()"

}