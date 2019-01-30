package io.openfuture.chain.smartcontract.util

import io.openfuture.chain.smartcontract.exception.SmartContractValidationException
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.objectweb.asm.ClassReader

class ByteCodeUtilsTest {

    @Test
    fun getClassNameTest() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/HelloContract.class").readBytes()

        assertThat(ClassReader(oldBytes).className.asPackagePath).isEqualTo("io.test.HelloContract")
    }

    @Test
    fun processByteArrayTest() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/HelloContract.class").readBytes()
        val newName = "Test"

        val newBytes = ByteCodeUtils.processByteArray(oldBytes, newName)

        assertThat(ClassReader(oldBytes).className.asPackagePath).isNotEqualTo(newName)
        assertThat(ClassReader(newBytes).className.asPackagePath).isEqualTo(newName)
    }

    @Test(expected = SmartContractValidationException::class)
    fun processByteArrayWhenClassIsNotSmartContract() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/HelloClass.class").readBytes()
        val newName = "Test"

        ByteCodeUtils.processByteArray(oldBytes, newName)
    }

    @Test(expected = SmartContractValidationException::class)
    fun processByteArrayWhenFieldIsNotCorrectType() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/JavaContractField.class").readBytes()
        val newName = "Test"

        ByteCodeUtils.processByteArray(oldBytes, newName)
    }

    @Test(expected = SmartContractValidationException::class)
    fun processByteArrayWhenMethodIsNotCorrectType() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/JavaContractMethod.class").readBytes()
        val newName = "Test"

        ByteCodeUtils.processByteArray(oldBytes, newName)
    }

}