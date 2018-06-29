package io.openfuture.chain.crypto.key

import org.junit.Assert
import org.junit.Test
import java.util.*

class PrivateKeyManagerTest {

    private val privateKeyManager = PrivateKeyManager()

    @Test
    fun exportPrivateKeyShouldReturnPrivateKeyInWIFFormat() {
        val deserializer = ExtendedKeyDeserializer()
        val key = deserializer.deserialize("xprv9s21ZrQH143K2XshqfF4THurU3nZJzE4oy8cn5Tkt95pTkmpXCN1UtZyR85ERwhvYuRzDuDkzqTVAPys4SRJc9qgUzpGnv6QcHmKLoH7ZxD")

        val exported = privateKeyManager.exportPrivateKey(key.ecKey)

        Assert.assertEquals(exported, "Kz5FUmSQf37sncxHS9LRGaUGokh9syGhwdZEFdYNX5y9uVZH8myo")
    }

    @Test
    fun importPrivateKeyShouldReturnECKeyFromWIFFormat() {
        val deserializer = ExtendedKeyDeserializer()
        val key = deserializer.deserialize("xprv9s21ZrQH143K2XshqfF4THurU3nZJzE4oy8cn5Tkt95pTkmpXCN1UtZyR85ERwhvYuRzDuDkzqTVAPys4SRJc9qgUzpGnv6QcHmKLoH7ZxD")

        val exported = privateKeyManager.exportPrivateKey(key.ecKey)
        val imported = privateKeyManager.importPrivateKey(exported)

        Assert.assertEquals(imported.private, key.ecKey.private)
        Assert.assertTrue(Arrays.equals(imported.public, key.ecKey.public))
    }

}
