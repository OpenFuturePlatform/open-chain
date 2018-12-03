package io.openfuture.chain.smartcontract.deploy

import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.load.SourceClassLoader
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassLoadingTests {
    
    @Test
    fun loadBytesWhenValidContract() {
        val path = "/classes/CalculatorContract.class"
        val bytes = getResource(path).toFile().readBytes()
        val loader = SourceClassLoader()

        val loaded = loader.loadBytes(ClassSource(bytes))

        assertThat(loaded.clazz.newInstance().javaClass.simpleName).isEqualTo("CalculatorContract")
        assertThat(loaded.byteCode).isEqualTo(bytes)
    }

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
    fun loadBytesWhenJavaClass() {
        val javaBytes = """
            cafebabe0000003400140a000400100800110700120700130100063c696e69743e010003282956010004436f646501000f4c696
            e654e756d6265725461626c650100124c6f63616c5661726961626c655461626c65010004746869730100134c696f2f74657374
            2f48656c6c6f4a6176613b01000568656c6c6f01001428294c6a6176612f6c616e672f537472696e673b01000a536f757263654
            6696c6501000e48656c6c6f4a6176612e6a6176610c0005000601000d48656c6c6f2c20776f726c6421010011696f2f74657374
            2f48656c6c6f4a6176610100106a6176612f6c616e672f4f626a656374002100030004000000000002000100050006000100070
            000002f00010001000000052ab70001b10000000200080000000600010000000300090000000c000100000005000a000b000000
            01000c000d000100070000002d00010001000000031202b00000000200080000000600010000000600090000000c00010000000
            3000a000b00000001000e00000002000f
        """

        val loader = SourceClassLoader()
        val clazz = loader.loadBytes("io.test.HelloJava", ByteUtils.fromHexString(javaBytes)).clazz
        val result = clazz.getDeclaredMethod("hello").invoke(clazz.newInstance())

        assertThat(result).isEqualTo("Hello, world!")
    }

    @Test
    fun loadBytesWhenKotlinClass() {
        val kotlinBytes = """
            cafebabe000000340024010013696f2f746573742f48656c6c6f4b6f746c696e0700010100106a6176612f6c616e672f4f626a65
            637407000301000568656c6c6f01001428294c6a6176612f6c616e672f537472696e673b0100234c6f72672f6a6574627261696e
            732f616e6e6f746174696f6e732f4e6f744e756c6c3b01000d48656c6c6f2c20776f726c6421080008010004746869730100154c
            696f2f746573742f48656c6c6f4b6f746c696e3b0100063c696e69743e0100032829560c000c000d0a0004000e0100114c6b6f74
            6c696e2f4d657461646174613b0100026d760300000001030000000d0100026276030000000003000000030100016b0100026431
            010032c080120a0218020a0210c0800a0208020a02100e0ac08018c080320230014205c2a2060210024a0610031a023004c2a806
            05010002643201000001000f6f70656e2d636861696e5f6d61696e01000e48656c6c6f4b6f746c696e2e6b74010004436f646501
            00124c6f63616c5661726961626c655461626c6501000f4c696e654e756d6265725461626c6501001b52756e74696d65496e7669
            7369626c65416e6e6f746174696f6e7301000a536f7572636546696c6501001952756e74696d6556697369626c65416e6e6f7461
            74696f6e730031000200040000000000020011000500060002001e0000002d00010001000000031209b000000002001f0000000c
            000100000003000a000b00000020000000060001000000050021000000060001000700000001000c000d0001001e0000002f0001
            0001000000052ab7000fb100000002001f0000000c000100000005000a000b000000200000000600010000000300020022000000
            02001d00230000004600010010000500115b000349001249001249001300145b0003490012490015490016001749001200185b00
            01730019001a5b000673000b73001b73000d73000573001b73001c
        """

        val loader = SourceClassLoader()
        val clazz = loader.loadBytes("io.test.HelloKotlin", ByteUtils.fromHexString(kotlinBytes)).clazz
        val result = clazz.getDeclaredMethod("hello").invoke(clazz.newInstance())

        assertThat(result).isEqualTo("Hello, world!")
    }

    private fun getResource(path: String): Path = Paths.get(javaClass.getResource(path).toURI())

}