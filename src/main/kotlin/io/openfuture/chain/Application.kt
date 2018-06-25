package io.openfuture.chain

import io.openfuture.chain.component.seed.validator.SeedPhraseValidator
import io.openfuture.chain.service.CryptoService
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
    private lateinit var cryptoService: CryptoService

    @Autowired
    private lateinit var seedPhraseValidator: SeedPhraseValidator

    override fun run(vararg args: String?) {
        val sp = cryptoService.generateSeedPhrase()
        seedPhraseValidator.validate(sp)
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