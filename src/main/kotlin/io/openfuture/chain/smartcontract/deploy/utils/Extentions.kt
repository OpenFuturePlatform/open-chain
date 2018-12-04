package io.openfuture.chain.smartcontract.deploy.utils

import java.net.URL
import java.nio.file.Path

fun Path.toURL(): URL = this.toUri().toURL()

val String.asPackagePath: String get() = this.replace('/', '.')
val String.asResourcePath: String get() = this.replace('.', '/')