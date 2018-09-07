package io.openfuture.chain.network.component

import io.openfuture.chain.network.entity.NetworkAddress
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ExplorerAddressesHolder {

    private val addresses = ConcurrentHashMap.newKeySet<NetworkAddress>()
    var me: NetworkAddress? = null


    fun getAddresses(): Set<NetworkAddress> {
        me?.let { return addresses.minus(it) }
        return addresses
    }

    @Synchronized
    fun addAddress(address: NetworkAddress) {
        this.addresses.add(address)
    }

    @Synchronized
    fun addAddresses(addresses: Set<NetworkAddress>) {
        this.addresses.addAll(addresses)
    }

    @Synchronized
    fun removeAddress(address: NetworkAddress) {
        addresses.remove(address)
    }

}