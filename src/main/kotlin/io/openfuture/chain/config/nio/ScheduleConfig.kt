package io.openfuture.chain.config.nio

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.annotation.EnableScheduling


@Configuration
@EnableScheduling
class SchedulerConfig : SchedulingConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 5
        taskScheduler.initialize()
        taskScheduler.threadNamePrefix = "ScheduledExecutor-"

        taskRegistrar.setTaskScheduler(taskScheduler)
    }

}
