package io.zensoft.config

import io.zensoft.service.UserService
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
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

    @MockBean
    protected lateinit var userService: UserService

}
