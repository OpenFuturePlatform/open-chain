package io.openfuture.chain.config

import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.util.AppContextUtils
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * See the
 * <a href="https://docs.spring.io/spring-security/site/docs/current/reference/html/test-method.html">documentation</a>
 */
@RunWith(SpringRunner::class)
@Import(NodeProperties::class, AppContextUtils::class)
abstract class ControllerTests {

    @Autowired
    protected lateinit var webClient: WebTestClient

}