package io.openfuture.chain.crypto.component.key

import io.openfuture.chain.config.ServiceTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedKeyDeserializerTests : ServiceTests() {

    private val deserializer = ExtendedKeyDeserializer()
    private val serializer = ExtendedKeySerializer()


    @Test
    fun deserializePrivateKeyShouldReturnDeserializedPublicAndPrivateKeysWhenDeserializeMasterKey() {
        val serializedKey = "xprv9s21ZrQH143K4QKw9Cq9BUSUJGMSNMBt5mQVU8QD32NZpw4i6bnmiACNkqunc6P6B5tHXGw4oJMo2wXVwDgj2WDQFpTFufd4TdtKpZvpgEb"
        val expectedPublicKey = "xpub661MyMwAqRbcGtQQFEN9YcPCrJBvmoujSzL6GWopbMuYhjPre972FxWrc6NHZiH87hAz3vg3o95GDTwncHF6dMkoJLQ897p4VssRDA4kJ7V"

        val extendedKey = deserializer.deserialize(serializedKey)

        assertThat(extendedKey.sequence).isEqualTo(0)
        assertThat(extendedKey.depth).isEqualTo(0)
        assertThat(serializer.serializePrivate(extendedKey)).isEqualTo(serializedKey)
        assertThat(serializer.serializePublic(extendedKey)).isEqualTo(expectedPublicKey)
    }

    @Test
    fun deserializePrivateKeyShouldReturnDeserializedPublicAndPrivateKeysWhenDeserializeDerivedKey() {
        val serializedKey = "xprv9y4QiLMBeJrBhVYEEKnFtHm7VT1TuW3Nt7ZfixrReWpaaWStJ6Ljc9txbh74wzgK1c7j91gu6nPZJoD8qGYRy2mwRmmnH8JpzHb9zzTgGyu"
        val expectedPublicKey = "xpub6C3m7qt5UgQUuychLMKGFRhr3UqxJxmEFLVGXMG3CrMZTJn2qdez9xDSSxoB5HLuX33mQ3JPia8ni2TP5GJ3vs7xBid1ZvgK1fkCd7T7dqH"

        val extendedKey = deserializer.deserialize(serializedKey)

        assertThat(extendedKey.sequence).isEqualTo(1)
        assertThat(extendedKey.depth).isEqualTo(3)
        assertThat(serializer.serializePrivate(extendedKey)).isEqualTo(serializedKey)
        assertThat(serializer.serializePublic(extendedKey)).isEqualTo(expectedPublicKey)
    }

    @Test
    fun deserializePublicKeyShouldReturnDeserializedPublicKeyWhenDeserializeMasterKey() {
        val serializedKey = "xpub661MyMwAqRbcGtQQFEN9YcPCrJBvmoujSzL6GWopbMuYhjPre972FxWrc6NHZiH87hAz3vg3o95GDTwncHF6dMkoJLQ897p4VssRDA4kJ7V"

        val extendedKey = deserializer.deserialize(serializedKey)

        assertThat(extendedKey.depth).isEqualTo(0)
        assertThat(extendedKey.ecKey.private).isNull()
        assertThat(serializer.serializePublic(extendedKey)).isEqualTo(serializedKey)
    }

    @Test
    fun deserializePublicKeyShouldReturnDeserializedPublicKeyWhenDeserializeDerivedKey() {
        val serializedKey = "xpub6C3m7qt5UgQUuychLMKGFRhr3UqxJxmEFLVGXMG3CrMZTJn2qdez9xDSSxoB5HLuX33mQ3JPia8ni2TP5GJ3vs7xBid1ZvgK1fkCd7T7dqH"

        val extendedKey = deserializer.deserialize(serializedKey)

        assertThat(extendedKey.depth).isEqualTo(3)
        assertThat(extendedKey.ecKey.private).isNull()
        assertThat(serializer.serializePublic(extendedKey)).isEqualTo(serializedKey)
    }

    @Test(expected = Exception::class)
    fun deserializeShouldThrowExceptionWhenIncorrectKey() {
        val serializedKey = "xpub68GHhbqGdkJSJ4Ly8"

        deserializer.deserialize(serializedKey)
    }

}