package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.util.AddressUtils
import org.springframework.stereotype.Component

/**
 * Mixed-case address checksum validator
 */
@Component
class AddressValidator {

    companion object {
        private const val ADDRESS_PATTERN = "^0x[0-9a-fA-F]{40}$"
    }

    fun validate(address: String): Boolean {
        val noPrefixAddress = AddressUtils.removePrefix(address)
        return if (isAddress(address))
            noPrefixAddress == AddressUtils.addChecksum(noPrefixAddress.toLowerCase())
        else false
    }

    private fun isAddress(address: String): Boolean = ADDRESS_PATTERN.toRegex().matches(address)

}