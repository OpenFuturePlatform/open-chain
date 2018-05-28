package io.zensoft.config.security

import io.zensoft.config.annotation.WithUser
import io.zensoft.entity.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class SecurityContextFactory : WithSecurityContextFactory<WithUser> {

    override fun createSecurityContext(customUser: WithUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()

        val principal = User(customUser.email, "name", 20)
        principal.addRoles(customUser.value.roles)

        context.authentication = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
        return context
    }

}

