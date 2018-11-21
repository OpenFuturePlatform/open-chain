package io.openfuture.chain.smartcontract.deploy.load

class Whitelist private constructor(
    private val entries: Set<Regex>
) {

    companion object {

        private val ALLOWED_CLASSES = setOf(
            "^java/lang/Class(\\..*)?\$".toRegex(),
            "^java/lang/ClassLoader(\\..*)?\$".toRegex(),
            "^java/lang/Cloneable(\\..*)?\$".toRegex(),
            "^java/lang/Object(\\..*)?\$".toRegex(),
            "^java/lang/Override(\\..*)?\$".toRegex(),
            "^java/lang/Void\$".toRegex(),
            "^java/lang/invoke/LambdaMetafactory\$".toRegex(),
            "^java/lang/invoke/MethodHandles(\\\$.*)?\$".toRegex(),
            "^java/lang/reflect/Array(\\..*)?\$".toRegex(),
            "^java/io/Serializable\$".toRegex()
        )

        val INSTANCE: Whitelist = Whitelist(ALLOWED_CLASSES)


    }

    fun matches(name: String): Boolean = entries.any { it.matches(name) }

}