package io.openfuture.chain.core.util

import io.openfuture.chain.core.model.entity.base.Dictionary

object DictionaryUtils {

    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<*>> valueOf(clazz: Class<out T>, id: Int): T =
        clazz.enumConstants.firstOrNull { (it as Dictionary).getId() == id }
            ?: throw IllegalStateException("Type ID not found")

}