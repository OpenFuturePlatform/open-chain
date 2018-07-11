package io.openfuture.chain.crypto.key

import io.openfuture.chain.config.ServiceTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedKeyDeserializerTests : ServiceTests() {

    private val deserializer = ExtendedKeyDeserializer()
    private val serializer = ExtendedKeySerializer()


    @Test
    fun deserializePrivateKeyShouldReturnDeserializedPublicAndPrivateKeys() {
        val serializedKey = "xprv9s21ZrQH143K4QKw9Cq9BUSUJGMSNMBt5mQVU8QD32NZpw4i6bnmiACNkqunc6P6B5tHXGw4oJMo2wXVwDgj2WDQFpTFufd4TdtKpZvpgEb"
        val expectedPublicKey = "xpub661MyMwAqRbcGtQQFEN9YcPCrJBvmoujSzL6GWopbMuYhjPre972FxWrc6NHZiH87hAz3vg3o95GDTwncHF6dMkoJLQ897p4VssRDA4kJ7V"

        val extendedKey = deserializer.deserialize(serializedKey)

        assertThat(extendedKey.depth).isEqualTo(0)
        assertThat(serializer.serializePrivate(extendedKey)).isEqualTo(serializedKey)
        assertThat(serializer.serializePublic(extendedKey)).isEqualTo(expectedPublicKey)
    }

    @Test
    fun deserializePublicKeyShouldReturnDeserializedPublicKey() {
        val serializedKey = "xpub661MyMwAqRbcGtQQFEN9YcPCrJBvmoujSzL6GWopbMuYhjPre972FxWrc6NHZiH87hAz3vg3o95GDTwncHF6dMkoJLQ897p4VssRDA4kJ7V"

        val extendedKey = deserializer.deserialize(serializedKey)

        assertThat(extendedKey.depth).isEqualTo(0)
        assertThat(extendedKey.ecKey.private).isNull()
        assertThat(serializer.serializePublic(extendedKey)).isEqualTo(serializedKey)
    }

    @Test(expected = Exception::class)
    fun deserializeShouldThrowExceptionWhenIncorrectKey() {
        val serializedKey = "xpub68GHhbqGdkJSJ4Ly8"

        deserializer.deserialize(serializedKey)
    }

}