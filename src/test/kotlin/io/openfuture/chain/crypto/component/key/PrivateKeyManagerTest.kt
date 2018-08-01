package io.openfuture.chain.crypto.component.key

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class PrivateKeyManagerTest {

    private val privateKeyManager = PrivateKeyManager()


    @Test
    fun exportPrivateKeyShouldReturnPrivateKeyInWifFormat() {
        val deserializer = ExtendedKeyDeserializer()
        val key = deserializer.deserialize("xprv9s21ZrQH143K2XshqfF4THurU3nZJzE4oy8cn5Tkt95pTkmpXCN1UtZyR85ERwhvYuRzDuDkzqTVAPys4SRJc9qgUzpGnv6QcHmKLoH7ZxD")

        val exported = privateKeyManager.exportPrivateKey(key.ecKey)

        assertThat(exported).isEqualTo("Kz5FUmSQf37sncxHS9LRGaUGokh9syGhwdZEFdYNX5y9uVZH8myo")
    }

    @Test
    fun importPrivateKeyShouldReturnECKeyFromWifFormat() {
        val deserializer = ExtendedKeyDeserializer()
        val key = deserializer.deserialize("xprv9s21ZrQH143K2XshqfF4THurU3nZJzE4oy8cn5Tkt95pTkmpXCN1UtZyR85ERwhvYuRzDuDkzqTVAPys4SRJc9qgUzpGnv6QcHmKLoH7ZxD")

        val exported = privateKeyManager.exportPrivateKey(key.ecKey)
        val imported = privateKeyManager.importPrivateKey(exported)

        assertThat(imported.private).isEqualTo(key.ecKey.private)
        assertThat(Arrays.equals(imported.public, key.ecKey.public)).isTrue()
    }

}
