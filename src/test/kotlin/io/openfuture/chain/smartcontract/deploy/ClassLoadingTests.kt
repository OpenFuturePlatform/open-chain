package io.openfuture.chain.smartcontract.deploy

import io.openfuture.chain.ResourceUtils.getResourceBytes
import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.exception.ContractLoadingException
import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClassLoadingTests {
    

    @Test
    fun loadBytesWhenValidJavaContractClass() {
        val javaBytes = getResourceBytes("/classes/JavaContract.class")
        val className = "io.test.JavaContract"
        val loader = SourceClassLoader()

        val clazz = loader.loadBytes(className, javaBytes).clazz
        val result = clazz.getDeclaredMethod("hello").invoke(clazz.newInstance())

        assertThat(clazz.name).isEqualTo(className)
        assertThat(result).isEqualTo("Hello, world!")
    }

    @Test
    fun loadBytesWhenValidKotlinContractClass() {
        val kotlinBytes = getResourceBytes("/classes/KotlinContract.class")
        val className = "io.test.KotlinContract"
        val loader = SourceClassLoader()

        val clazz = loader.loadBytes(className, kotlinBytes).clazz
        val result = clazz.getDeclaredMethod("hello").invoke(clazz.newInstance())

        assertThat(clazz.name).isEqualTo(className)
        assertThat(result).isEqualTo("Hello, world!")
    }

    @Test(expected = ContractLoadingException::class)
    fun loadBytesWhenJavaClassIsNotContractShouldThrowContractLoadingException() {
        val bytes = getResourceBytes("/classes/HelloClass.class")
        val loader = SourceClassLoader()

        loader.loadBytes(ClassSource(bytes))
    }

    @Test(expected = ContractLoadingException::class)
    fun loadBytesWhenJavaClassContainsInvalidFieldTypeShouldThrowContractLoadingException() {
        val bytes = getResourceBytes("/classes/JavaContractField.class")
        val loader = SourceClassLoader()

        loader.loadBytes(ClassSource(bytes))
    }

    @Test(expected = ContractLoadingException::class)
    fun loadBytesWhenJavaClassContainsInvalidMethodReturnTypeShouldThrowContractLoadingException() {
        val bytes = getResourceBytes("/classes/JavaContractMethod.class")
        val loader = SourceClassLoader()

        loader.loadBytes(ClassSource(bytes))
    }

    @Test(expected = ContractLoadingException::class)
    fun loadBytesWhenJavaClassContainsThreadAndThreadDeathExceptionShouldThrowContractLoadingException() {
        val bytes = getResourceBytes("/classes/JavaContractThread.class")
        val loader = SourceClassLoader()

        loader.loadBytes(ClassSource(bytes))
    }

}