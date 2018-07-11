package io.openfuture.chain

import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.TransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@SpringBootApplication
class Application : CommandLineRunner {

    @Autowired
    private lateinit var blockService: BlockService

    override fun run(vararg args: String?) {
        val t1 = Transaction(
            "h",
            1,
            1L,
            "rk",
            "sk",
            "s",
            null
        )
        val t2 = Transaction(
            "h",
            1,
            1L,
            "rk",
            "sk",
            "s",
            null
        )

        val block = MainBlock("1", 2, "3", "4", 5L, "6", listOf(t1, t2))

        blockService.save(block)
    }

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