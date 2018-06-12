package io.openfuture.chain.config

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

/**
 * See the
 * <a href="https://docs.spring.io/spring-security/site/docs/current/reference/html/test-method.html">documentation</a>
 */
@RunWith(SpringRunner::class)
abstract class ControllerTests {

    @Autowired
    protected lateinit var mvc: MockMvc

}
