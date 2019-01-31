package io.openfuture.chain.smartcontract.util

val String.asPackagePath: String get() = this.replace('/', '.')

val String.asResourcePath: String get() = this.replace('.', '/')

fun Set<String>.toOrRegex(): Regex = this.joinToString(separator = "|") { "(.*$it.*)" }.toRegex()