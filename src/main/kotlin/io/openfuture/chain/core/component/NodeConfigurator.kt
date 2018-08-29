package io.openfuture.chain.core.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.text.Charsets.UTF_8

@Component
class NodeConfigurator(
    properties: NodeProperties
) {

    private val file = File(properties.configPath)
    private val mapper = ObjectMapper().registerModule(KotlinModule())
    private var config = NodeConfig(properties.port!!)


    @PostConstruct
    private fun init() {
        mapper.enable(INDENT_OUTPUT)

        if (!file.exists()) {
            file.createNewFile()
            file.writeText(mapper.writeValueAsString(config), UTF_8)
        } else {
            config = mapper.readValue(file, NodeConfig::class.java)
        }

    }

    @PreDestroy
    private fun destroy() {
        file.writeText(mapper.writeValueAsString(config), UTF_8)
    }

    fun getConfig(): NodeConfig = config

    @NoArgConstructor
    data class NodeConfig(
        var externalPort: Int,
        var externalHost: String = "",
        var secret: String = ""
    )
}

