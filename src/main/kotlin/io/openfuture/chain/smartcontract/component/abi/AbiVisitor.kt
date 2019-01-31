package io.openfuture.chain.smartcontract.component.abi

import io.openfuture.chain.smartcontract.component.abi.domain.Abi
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM6
import java.lang.reflect.Modifier

class AbiVisitor : ClassVisitor(ASM6) {

    val abi = Abi()


    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?,
                             exceptions: Array<out String>?): MethodVisitor {
        if (Modifier.isPublic(access)) {
            name?.let { abi.addMethodName(name) }
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

}