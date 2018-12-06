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
    private val map: Map<String, Set<String>> = mapOf(
        "java.lang" to setOf(
            "invoke",
            "*Thread*"
        )
    )

    private val allowedNamespaces = setOf(
        "java.lang.",
        "java.util.",
        "io.openfuture.chain.smartcontract."
    )

    private val blackListedEntries = setOf(
        "java\\.awt\\..*",
        "java\\.beans\\..*",
        "java\\.lang\\.invoke\\..*",
        "java\\.lang\\..*Thread.*",
        "java\\.lang\\.Shutdown.*",
        "java\\.lang\\.ref\\..*",
        "java\\.lang\\.reflect\\.InvocationHandler.*",
        "java\\.lang\\.reflect\\.Proxy.*",
        "java\\.lang\\.reflect\\.Weak.*",
        "java\\.io\\..*File.*",
        "java\\.net\\..*Content.*",
        "java\\.net\\.Host.*",
        "java\\.net\\.Inet.*",
        "java\\.nio\\.file\\..*",
        "java\\.util\\..*Random.*",
        "java\\.util\\.WeakHashMap.*",
        "java\\.util\\.concurrent\\..*",
        "java\\.util\\.concurrent\\.locks\\..*",
        "javax\\.activation\\..*"
    ).map { it.toRegex() }


    fun isAllowed(className: String): Boolean {
        if (isPrimitive(className)) {
            return true
        }

        return allowedNamespaces.any { className.startsWith(it) } && blackListedEntries.none { it.matches(className) }
    }

    private fun isPrimitive(className: String): Boolean = primitives.contains(className)

}