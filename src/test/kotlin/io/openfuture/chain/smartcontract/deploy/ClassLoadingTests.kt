package io.openfuture.chain.smartcontract.deploy

import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassLoadingTests {

    @Test
    fun loadClassFromFile() {
        val path = "/classes/CalculatorContract.class"
        val loader = SourceClassLoader(listOf(getResource(path)))

        val clazz = loader.loadClass("io.openfuture.chain.smartcontract.templates.CalculatorContract")
        val contract = clazz.newInstance()

        assertThat(contract).isNotNull
        assertThat(clazz.getDeclaredMethod("result").invoke(contract)).isEqualTo(0L)
        clazz.getDeclaredMethod("add", Long::class.java).invoke(contract, 10L)
        assertThat(clazz.getDeclaredMethod("result").invoke(contract)).isEqualTo(10L)
    }


    @Test
    fun loadBytesWhenValidJavaContractClass() {
        val javaBytes = getResource("/classes/JavaContract.class").toFile().readBytes()
        val className = "io.test.JavaContract"
        val loader = SourceClassLoader()

        val clazz = loader.loadBytes(className, javaBytes).clazz
        val result = clazz.getDeclaredMethod("hello").invoke(clazz.newInstance())

        assertThat(clazz.name).isEqualTo(className)
        assertThat(result).isEqualTo("Hello, world!")
    }

    @Test
    fun loadBytesWhenValidKotlinContractClass() {
        val kotlinBytes = getResource("/classes/KotlinContract.class").toFile().readBytes()
        val className = "io.test.KotlinContract"

        val loader = SourceClassLoader()

        val clazz = loader.loadBytes(className, kotlinBytes).clazz
        val result = clazz.getDeclaredMethod("hello").invoke(clazz.newInstance())

        assertThat(clazz.name).isEqualTo(className)
        assertThat(result).isEqualTo("Hello, world!")
    }

    private fun getResource(path: String): Path = Paths.get(javaClass.getResource(path).toURI())

}