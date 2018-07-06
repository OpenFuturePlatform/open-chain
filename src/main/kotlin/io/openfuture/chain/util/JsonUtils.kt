package io.openfuture.chain.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class JsonUtils : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        mapper = applicationContext.getBean(ObjectMapper::class.java).copy()
        mapper!!.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
    }

    companion object {

        private var mapper: ObjectMapper? = null

        fun toJson(`object`: Any?): String? {
            return if (null == `object`) null else mapper!!.writeValueAsString(`object`)
        }

        fun <T> fromJson(json: String?, clazz: Class<T>): T? {
            return if (null == json) null else mapper!!.readValue(json, clazz)
        }

        fun <T> fromJson(json: String?, type: TypeReference<T>): T? {
            return if (null == json) null else mapper!!.readValue<T>(json, type)
        }

    }

}
