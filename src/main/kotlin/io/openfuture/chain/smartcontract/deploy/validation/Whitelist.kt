package io.openfuture.chain.smartcontract.deploy.validation

object Whitelist {

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

    // todo package to exceptions from packages
    private val map: Map<String, Regex> = mapOf(
        "java.lang." to setOf(
            "invoke.",
            "ref.",
            "reflect.",

            ".*Thread",
            "Shutdown",
            "ClassLoader"
        ).toOrRegex(),

        "java.util." to setOf(
            "concurrent.",
            "logging.",
            "prefs.",
            "jar.",

            "UUID",
            ".*Random",
            "WeakHashMap",
            "Timer",
            "Scanner"
        ).toOrRegex(),

        "io.openfuture.chain.smartcontract." to Regex("")
    )

    private val blacklistedExceptions = setOf(
        "java.lang.StackOverflowError",
        "java.lang.OutOfMemoryError",
        "java.lang.VirtualMachineError",
        "java.lang.ThreadDeath",
        "java.lang.Throwable",
        "java.lang.Error"
    )


    fun isAllowed(className: String): Boolean {
        if (isPrimitive(className)) {
            return true
        }

        for (rootPackage in map.keys) {
            if (className.startsWith(rootPackage)) {
                return !map[rootPackage]!!.matches(className.removePrefix(rootPackage))
            }
        }

        return false
    }

    fun isAllowedException(className: String): Boolean = !blacklistedExceptions.contains(className)

    private fun isPrimitive(className: String): Boolean = primitives.contains(className)

    private fun Set<String>.toOrRegex(): Regex = this.joinToString(separator = "|") { "($it.*)" }.toRegex()

}