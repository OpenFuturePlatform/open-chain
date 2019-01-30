package io.openfuture.chain.smartcontract.util

import io.openfuture.chain.smartcontract.exception.SmartContractValidationException
import io.openfuture.chain.smartcontract.validation.SmartContractValidator
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.slf4j.LoggerFactory

object ByteCodeUtils {

    fun getClassName(bytes: ByteArray): String = ClassReader(bytes).className

    fun processByteArray(bytes: ByteArray, newName: String): ByteArray {
        val reader = ClassReader(bytes)

        SmartContractValidator().use {
            reader.accept(it, ClassReader.SKIP_DEBUG)

            val result = it.validationResult
            if (result.hasErrors()) {
                log.warn(result.getErrors().joinToString("\n\n"))
                throw SmartContractValidationException("Contract class is invalid")
            }
        }

        val writer = ClassWriter(0)
        val oldName = reader.className.asResourcePath

        val adapter = ClassRemapper(writer, SimpleRemapper(oldName, newName))
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)

        return writer.toByteArray()
    }

    private val log = LoggerFactory.getLogger(ByteCodeUtils::class.java)

}