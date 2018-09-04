package io.openfuture.chain.network.component

import io.openfuture.chain.network.entity.NetworkAddress
import org.springframework.stereotype.Component

@Component
class ExplorerAddressesHolder {

    private val addresses = mutableSetOf<NetworkAddress>()


    fun getAddresses(): Set<NetworkAddress> = HashSet(addresses)

    @Synchronized
    fun addAddress(address: NetworkAddress) {
        this.addresses.add(address)
    }

    @Synchronized
    fun addAddresses(addresses: Set<NetworkAddress>) {
        this.addresses.addAll(addresses)
    }

    fun removeAddress(address: NetworkAddress) {
        addresses.remove(address)
    }

}