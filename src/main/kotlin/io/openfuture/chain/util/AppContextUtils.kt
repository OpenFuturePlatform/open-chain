package io.openfuture.chain.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
object AppContextUtils : ApplicationContextAware {

    lateinit var context: ApplicationContext

    fun <T> getBean(type: Class<T>): T {
        return context.getBean(type)
    }

    fun getUpTime(): Long = System.currentTimeMillis() - context.startupDate

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }

}