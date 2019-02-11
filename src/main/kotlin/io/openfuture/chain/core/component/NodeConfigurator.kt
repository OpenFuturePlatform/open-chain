package io.openfuture.chain.core.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.LIGHT
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
            updateFile()
        } else {
            config = mapper.readValue(file, NodeConfig::class.java)
        }

    }

    @PreDestroy
    private fun destroy() {
        updateFile()
    }

    fun getConfig(): NodeConfig = config

    fun setExternalHost(host: String) {
        config.externalHost = host
        updateFile()
    }

    fun setSecret(secret: String) {
        config.secret = secret
        updateFile()
    }

    fun setMode(syncMode: SyncMode) {
        config.mode = syncMode
        updateFile()
    }

    private fun updateFile() {
        file.writeText(mapper.writeValueAsString(config), UTF_8)
    }

    @NoArgConstructor
    data class NodeConfig(
        var externalPort: Int,
        var externalHost: String = "",
        var secret: String = "",
        var mode: SyncMode = LIGHT
    )

}

