package io.zensoft.controller

import io.zensoft.config.ControllerTests
import io.zensoft.config.annotation.AccountType
import io.zensoft.config.annotation.WithUser
import io.zensoft.config.any
import io.zensoft.domain.UserDtoSearchRequest
import io.zensoft.domain.base.PageRequest
import io.zensoft.entity.user.Role
import io.zensoft.entity.user.User
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerTest : ControllerTests() {

    @Test
    @WithUser(AccountType.USER)
    fun getAllTest() {
        val user = User("email", "Name", 19)
        user.id = 1
        user.addRole(Role.Dictionary.USER)

        given(userService.getUsers(any(UserDtoSearchRequest::class.java), any(PageRequest::class.java)))
                .willReturn(PageImpl<User>(listOf(user)))

        mvc.perform(get("/api/users")
                .param("limit", "2"))
                .andExpect(status().isOk)
                .andExpect(content().json("{\"totalCount\":1,\"list\":[{\"email\":\"email\",\"name\":" +
                        "\"Name\",\"age\":19,\"authorities\":[{\"id\":2,\"key\":\"ROLE_USER\",\"authority\":" +
                        "\"ROLE_USER\"}],\"id\":1,\"username\":\"email\",\"enabled\":true,\"credentialsNonExpired" +
                        "\":true,\"accountNonExpired\":true,\"accountNonLocked\":true}]}"))
    }

}