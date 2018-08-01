package io.openfuture.chain.core.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
object AppContextUtils : ApplicationContextAware {

    lateinit var context: ApplicationContext


    fun <T> getBean(type: Class<T>): T {
        return context.getBean(type)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

}