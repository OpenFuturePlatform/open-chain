package io.openfuture.chain.smartcontract.deploy.validation

object BlackList {

    private val entries = setOf(
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
        "java\\.util\\.SplittableRandom.*",
        "java\\.util\\.Random.*",
        "java\\.util\\.WeakHashMap.*",
        "java\\.util\\.concurrent\\..*",
        "java\\.util\\.concurrent\\.locks\\..*",
        "javax\\.activation\\..*"
    ).map { it.toRegex() }

    fun matches(className: String): Boolean = entries.any { it.matches(className) }

}