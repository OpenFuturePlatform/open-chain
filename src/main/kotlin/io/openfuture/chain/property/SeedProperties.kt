package io.openfuture.chain.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@Component
@Validated
@ConfigurationProperties(value = "seed")
class SeedProperties {

    /** Path to words dictionary to generate seed phrase */
    @field:NotNull
    var dictionaryPath: String? = null

}
