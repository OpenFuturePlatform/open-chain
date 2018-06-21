package io.openfuture.chain.config

import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.util.AppContextUtils
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * See the
 * <a href="https://docs.spring.io/spring-security/site/docs/current/reference/html/test-method.html">documentation</a>
 */
@RunWith(SpringRunner::class)
@Import(NodeProperties::class)
abstract class ControllerTests {

    @MockBean
    private lateinit var context: ApplicationContext

    @Autowired
    private lateinit var nodeProperties: NodeProperties

    @Autowired
    protected lateinit var webClient: WebTestClient

    @Before
    fun setUp() {
        AppContextUtils.context = context
        given(AppContextUtils.getBean(NodeProperties::class.java)).willReturn(nodeProperties)
    }
}
