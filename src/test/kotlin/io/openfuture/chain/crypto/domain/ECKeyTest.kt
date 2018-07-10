package io.openfuture.chain.crypto.domain

import io.openfuture.chain.crypto.key.ExtendedKeyDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ECKeyTest {

    @Test
    fun getAddressShouldReturnAddressWithMixedCaseCheckSum() {
        val deserializer = ExtendedKeyDeserializer()
        val xpub = "xpub661MyMwAqRbcEnKbXcCqD2GT1di5zQxVqoHPAgHNe8dv5JP8gWmDproS6kFHJnLZd23tWevhdn4urGJ6b264DfTGKr8zjmYDjyDTi9U7iyT"
        val ecKey = deserializer.deserialize(xpub).ecKey

        val address = ecKey.getAddress()

        assertThat(address).isNotBlank()
        assertThat(address).isEqualTo("0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36e")
    }

}