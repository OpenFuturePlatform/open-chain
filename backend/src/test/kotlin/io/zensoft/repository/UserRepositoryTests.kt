package io.zensoft.repository

import io.zensoft.config.RepositoryTests
import io.zensoft.entity.user.User
import org.assertj.core.api.Assertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class UserRepositoryTests: RepositoryTests(){

    @Autowired
    private lateinit var repository: UserRepository


    @Test
    fun findByEmailTest() {
        val expectedEmail = UUID.randomUUID().toString()
        val user = User(expectedEmail, "Name", 19)
        entityManager.persist(user)

        val account = repository.findByEmail(expectedEmail)
        Assertions.assertThat(account!!.email).isEqualTo(expectedEmail)
    }

}