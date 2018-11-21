package io.openfuture.chain.smartcontract.deploy.load

import io.openfuture.chain.smartcontract.deploy.domain.ClassSource
import io.openfuture.chain.smartcontract.deploy.domain.ClassSource.Companion.isClass
import io.openfuture.chain.smartcontract.deploy.domain.LoadedClass
import io.openfuture.chain.smartcontract.deploy.exception.ClassLoadingException
import io.openfuture.chain.smartcontract.deploy.utils.asResourcePath
import io.openfuture.chain.smartcontract.deploy.utils.toURL
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class SourceClassLoader(
    paths: List<Path> = emptyList()
) : URLClassLoader(resolvePaths(paths), getSystemClassLoader()) {

    private val classes: MutableMap<String, LoadedClass> = mutableMapOf()


    override fun loadClass(name: String): Class<*> = loadClass(name, false)

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        //todo validate
        return super.loadClass(name, resolve)
    }

    override fun findClass(name: String): Class<*> {
        val loadedClass = classes[name]
        if (null == loadedClass) {
            val bytes = readClassBytes(name)
            val clazz = loadBytes(name, bytes).clazz
            classes[name] = LoadedClass(clazz, bytes)
            resolveClass(clazz)
            return clazz
        }

        return loadedClass.clazz
    }

    fun loadBytes(className: String, bytes: ByteArray): LoadedClass =
        LoadedClass(defineClass(className, bytes, 0, bytes.size), bytes)

    // Load class by path todo: check is class
    fun loadClassFromPath(source: ClassSource): LoadedClass {
        addURL(source.path.parent.toURL())
        loadClass(source.name)
        return classes[source.name]!!
    }

    private fun readClassBytes(fullyQualifiedClassName: String): ByteArray {
        try {
            return (getResourceAsStream("${fullyQualifiedClassName.asResourcePath}.class")
                ?: throw ClassLoadingException("Class not found $fullyQualifiedClassName")).readBytes()
        } catch (e: IOException) {
            throw ClassLoadingException("Error reading bytecode", e)
        }
    }

}


private val homeDirectory: Path
    get() = Paths.get(System.getProperty("user.home"))


private fun resolvePaths(paths: List<Path>): Array<URL> = paths.map { expandPath(it) }.flatMap { path ->
    when {
        !Files.exists(path) -> throw FileNotFoundException("File not found; $path")
        isClass(path) || Files.isDirectory(path) -> listOf(path.toURL())
        else -> throw IllegalArgumentException("Expected a class file, but found $path")
    }
}.toTypedArray()

private fun expandPath(path: Path): Path = if (path.toString().startsWith("~/")) {
    homeDirectory.resolve(path.toString().removePrefix("~/"))
} else path

