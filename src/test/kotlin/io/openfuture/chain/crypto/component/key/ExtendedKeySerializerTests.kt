package io.openfuture.chain.crypto.component.key

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedKeySerializerTests : ServiceTests() {

    private val serializer = ExtendedKeySerializer()


    @Test
    fun serializePublicTestShouldReturnSerializedPublicKey() {
        val ecKey = ECKey("""xpub68GHhbqGdkJSJ4Ly8C1furjPtHAdDUzAzaVQzVJavjLt2DvvKYq
            t9UdcNrmk6JKU8h1rK2nWAV6yqPV6Hpvuf33dezACzKmFEbK3fWN4Za6""".toByteArray())
        val extendedKey = ExtendedKey(
            keyHash = ByteArray(64),
            ecKey = ecKey,
            chainCode = "d354d8b75cdc7b03d3af916cf3e18f00aaca455526322bf1e0352d12de7e7155".toByteArray()
        )

        val serializedExtendedKey = serializer.serializePublic(extendedKey)

        assertThat(serializedExtendedKey).isNotNull()
    }

    @Test
    fun serializePrivateTestShouldReturnSerializedPrivateKey() {
        val ecKey = ECKey(("""xpub68GHhbqGdzevrk1tp6iue1kXNHKqWnqevMYjBH9d3kmqfDgPyLVsMy
            SVsbDZfF9Uq8wmb5uzBW2wAbTpTEjLWbCnvWeyMFkaMNqe9Z8j43v""".toByteArray()))
        val extendedKey = ExtendedKey(
            keyHash = ByteArray(64),
            ecKey = ecKey,
            chainCode = "d354d8b75cdc7b03d3af916cf3e18f00aaca455526322bf1e0352d12de7e7155".toByteArray()
        )

        val serializedExtendedKey = serializer.serializePrivate(extendedKey)

        assertThat(serializedExtendedKey).isNotNull()
    }

}