package io.openfuture.chain.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Component
@Validated
@ConfigurationProperties(prefix = "delegates")
class DelegateProperties(

    /** The count of active delegates */
    @field:NotNull
    var count: Int? = null,

    /** The address of master nodes */
    @field:NotNull
    var address: String? = null,

    /** The public keys of master node */
    @field:NotEmpty
    var publicKeys: List<String>? = null

)