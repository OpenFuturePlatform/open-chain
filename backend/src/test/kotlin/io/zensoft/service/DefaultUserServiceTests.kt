package io.zensoft.service

import com.querydsl.core.BooleanBuilder
import io.zensoft.config.ServiceTests
import io.zensoft.domain.UserDtoSearchRequest
import io.zensoft.domain.base.PageRequest
import io.zensoft.entity.user.Role
import io.zensoft.entity.user.User
import io.zensoft.exception.NotFoundException
import io.zensoft.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.data.domain.PageImpl

class DefaultUserServiceTests : ServiceTests() {

    @Mock
    private lateinit var userRepository: UserRepository
    private lateinit var service: UserService

    @Before
    fun setUp() {
        service = DefaultUserService(userRepository)
    }

    @Test
    fun getByEmailTest() {
        val expectedUser = createUser()

        given(userRepository.findByEmail(expectedUser.email)).willReturn(expectedUser)

        val actualAccount = service.getByEmail(expectedUser.email)

        assertThat(actualAccount).isEqualTo(expectedUser)
    }

    @Test(expected = NotFoundException::class)
    fun getByEmailWhenUserNotFountShouldThrowNotFoundException() {
        val expectedUser = createUser()

        given(userRepository.findByEmail(expectedUser.email)).willReturn(null)

        service.getByEmail(expectedUser.email)
    }

    @Test
    fun getUsersTest() {
        val expectedUser = createUser()

        given(userRepository.findAll(BooleanBuilder(), PageRequest())).willReturn(PageImpl(listOf(expectedUser)))

        val actualUser = service.getUsers(UserDtoSearchRequest(), PageRequest())

        assertThat(actualUser.content).isEqualTo(listOf(expectedUser))
    }

    private fun createUser(): User {
        val user = User("email", "firstName", 19)
        user.id = 1
        user.addRole(Role.Dictionary.USER)

        return user
    }
}