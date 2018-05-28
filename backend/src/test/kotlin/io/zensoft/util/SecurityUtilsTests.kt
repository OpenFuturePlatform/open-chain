package io.zensoft.util

import io.zensoft.config.ServiceTests
import io.zensoft.entity.user.Role
import io.zensoft.entity.user.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.security.core.context.SecurityContextHolder

class SecurityUtilsTests : ServiceTests() {

    @Test
    fun updatePrincipalTest() {
        val role = Role(1, "ROLE_CUSTOMER")
        val account = User("email", "fn", 19)
        account.addRole(role)

        SecurityUtils.updatePrincipal(account)
        val principal = SecurityContextHolder.getContext().authentication.principal as User

        assertThat(principal).isEqualTo(account)
        assertThat(principal.authorities).contains(role)
    }

}
