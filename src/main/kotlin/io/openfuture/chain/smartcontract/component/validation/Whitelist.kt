package io.openfuture.chain.smartcontract.component.validation

import io.openfuture.chain.smartcontract.util.toOrRegex

object Whitelist {

    fun isAllowedType(className: String): Boolean {
        if (primitives.contains(className)) {
            return true
        }

        for (rootPackage in whiteListPackagesWithExceptions.keys) {
            if (className.startsWith(rootPackage)) {
                return !whiteListPackagesWithExceptions[rootPackage]!!.matches(className)
            }
        }

        return false
    }

    fun isAllowedException(className: String): Boolean = !blacklistedExceptions.contains(className)

    private val primitives = setOf(
        Boolean::class.java.name,
        Char::class.java.name,
        Byte::class.java.name,
        Short::class.java.name,
        Int::class.java.name,
        Long::class.java.name,
        Float::class.java.name,
        Double::class.java.name,
        Void::class.javaPrimitiveType!!.name
    )

    private val blacklistedExceptions = setOf(
        "java.lang.StackOverflowError",
        "java.lang.OutOfMemoryError",
        "java.lang.VirtualMachineError",
        "java.lang.ThreadDeath",
        "java.lang.Throwable",
        "java.lang.Error"
    )

    private val whiteListPackagesWithExceptions: Map<String, Regex> = mapOf(
        "java.lang." to setOf(
            "invoke.",
            "ref.",
            "reflect.",
            "management.",

            ".Thread",
            ".Process",
            ".ProcessBuilder",
            ".UNIXProcess",
            ".Shutdown",
            ".Math",
            ".ClassLoader"
        ).toOrRegex(),

        "java.util." to setOf(
            "concurrent.",
            "logging.",
            "prefs.",
            "jar.",
            "spi.",
            "zip.",

            ".UUID",
            ".Random",
            ".WeakHashMap",
            ".Timer",
            ".Scanner"
        ).toOrRegex(),

        "io.openfuture.chain.smartcontract." to setOf("").toOrRegex()
    )

}