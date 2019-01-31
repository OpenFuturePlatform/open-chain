package io.openfuture.chain.smartcontract.component.abi

import io.openfuture.chain.smartcontract.model.Abi
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM6
import java.lang.reflect.Modifier

class AbiVisitor : ClassVisitor(ASM6) {

    val abi = Abi()

    companion object {
        private const val INIT_METHOD = "<init>"
    }


    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?,
                             exceptions: Array<out String>?): MethodVisitor {
        if (Modifier.isPublic(access) && name!! != INIT_METHOD) {
            abi.addMethodName(name)
        }

        return object : MethodVisitor(ASM6) {}
    }

}