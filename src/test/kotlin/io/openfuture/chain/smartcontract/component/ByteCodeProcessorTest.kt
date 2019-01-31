package io.openfuture.chain.smartcontract.component

import io.openfuture.chain.smartcontract.exception.SmartContractValidationException
import io.openfuture.chain.smartcontract.util.asPackagePath
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.objectweb.asm.ClassReader

class ByteCodeProcessorTest {

    @Test
    fun getClassNameTest() {
        val bytes = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()

        assertThat(ClassReader(bytes).className.asPackagePath).isEqualTo("io.openfuture.chain.test.JavaContract")
    }

    @Test
    fun processByteArrayTest() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/JavaContract.class").readBytes()
        val newName = "Test"

        val newBytes = ByteCodeProcessor.processByteArray(oldBytes, newName)

        assertThat(ClassReader(oldBytes).className.asPackagePath).isNotEqualTo(newName)
        assertThat(ClassReader(newBytes).className.asPackagePath).isEqualTo(newName)
    }

    @Test(expected = SmartContractValidationException::class)
    fun processByteArrayWhenClassIsNotSmartContract() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/HelloClass.class").readBytes()
        val newName = "Test"

        ByteCodeProcessor.processByteArray(oldBytes, newName)
    }

    @Test(expected = SmartContractValidationException::class)
    fun processByteArrayWhenFieldIsNotCorrectType() {
        val oldBytes = this::class.java.getResourceAsStream("/classes/JavaContractField.class").readBytes()
        val newName = "Test"

        ByteCodeProcessor.processByteArray(oldBytes, newName)
    }

}