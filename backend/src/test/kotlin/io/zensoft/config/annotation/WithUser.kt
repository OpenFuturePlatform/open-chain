package io.zensoft.config.annotation

import io.zensoft.config.security.SecurityContextFactory
import io.zensoft.entity.user.Role
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, CLASS, FILE)
@Retention(RUNTIME)
@Inherited
@WithSecurityContext(factory = SecurityContextFactory::class)
annotation class WithUser(
        val value: AccountType,
        val email: String = "local@host"
)

enum class AccountType(vararg val roles: Role.Dictionary) {
    ADMIN(Role.Dictionary.ADMIN),
    USER(Role.Dictionary.USER)
}
