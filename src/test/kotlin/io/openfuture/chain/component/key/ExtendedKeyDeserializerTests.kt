package io.openfuture.chain.component.key

import io.openfuture.chain.config.ServiceTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedKeyDeserializerTests : ServiceTests() {

    private val deserializer = ExtendedKeyDeserializer()


    @Test
    fun deserializePrivateKeyTest() {
        val deserialzedKey = "xprv9uGwJ6JNoNk95aGW2AUfYinfLFL8p2GKdMZpC6tyNPou9Rbmn1XdbgK8XZgf44sECrD4vBDKPArGRrTeAWj9ThZWVb4qtFCMZEp4qF8tNfE"

        val extendedKey = deserializer.deserialize(deserialzedKey)

        assertThat(extendedKey.depth).isEqualTo(1)
        assertThat(extendedKey.ecKey!!.public).isNotNull()
        assertThat(extendedKey.ecKey!!.private).isNotNull()
    }

    @Test
    fun deserializePublicKeyTest() {
        val deserialzedKey = "xpub68GHhbqGdkJSJ4Ly8C1furjPtHAdDUzAzaVQzVJavjLt2DvvKYqt9UdcNrmk6JKU8h1rK2nWAV6yqPV6Hpvuf33dezACzKmFEbK3fWN4Za6"

        val extendedKey = deserializer.deserialize(deserialzedKey)

        assertThat(extendedKey.depth).isEqualTo(1)
        assertThat(extendedKey.ecKey!!.public).isNotNull()
        assertThat(extendedKey.ecKey!!.private).isNull()
    }

    @Test(expected = Exception::class)
    fun deserializeWhenIncorrectKeyShoulThrowExceptionTest() {
        val deserializedKey = "xpub68GHhbqGdkJSJ4Ly8"

        deserializer.deserialize(deserializedKey)
    }

}