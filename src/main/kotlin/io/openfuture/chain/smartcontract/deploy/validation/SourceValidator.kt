package io.openfuture.chain.smartcontract.deploy.validation

import io.openfuture.chain.smartcontract.core.model.SmartContract
import io.openfuture.chain.smartcontract.deploy.utils.asPackagePath
import io.openfuture.chain.smartcontract.deploy.utils.asResourcePath
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM6
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SourceValidator(val result: ValidationResult) : ClassVisitor(ASM6) {

    companion object {
        private val superContractName = SmartContract::class.java.name.asResourcePath
        private val log: Logger = LoggerFactory.getLogger(SourceValidator::class.java)
    }


    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        if (null == superName || superName != superContractName) {
            result.addError("Class is not a smart contract. Should inherit an ${superContractName.asPackagePath} class")
        }

        log.debug("CLASS: name-$name, interfaces-$interfaces, signature-$signature, version-$version")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor? {
        if (null != descriptor && BlackList.matches(descriptor)) {
            result.addError("$descriptor is forbidden to use in a contract")
        }

        log.debug("FIELD: name-$name, descriptor-$descriptor, signature-$signature")
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (null != descriptor && BlackList.matches(descriptor)) {
            result.addError("$descriptor is forbidden to use in a contract")
        }

        log.debug("METHOD: name-$name, descriptor-$descriptor, signature-$signature, exceptions-$exceptions")
        return SourceMethodVisitor()
    }


    private inner class SourceMethodVisitor : MethodVisitor(ASM6) {

        override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
            if (null != descriptor && BlackList.matches(descriptor)) {
                result.addError("$descriptor is forbidden to use in a contract's method")
            }

            super.visitLocalVariable(name, descriptor, signature, start, end, index)
        }

    }
}
