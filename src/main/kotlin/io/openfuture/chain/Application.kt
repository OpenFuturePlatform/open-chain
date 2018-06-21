package io.openfuture.chain

import org.apache.commons.io.FileUtils
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import java.io.File

@SpringBootApplication
class Application {

    @Profile("local")
    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            flyway.clean()
            flyway.migrate()
        }
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}