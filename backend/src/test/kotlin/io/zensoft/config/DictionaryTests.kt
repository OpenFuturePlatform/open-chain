package io.zensoft.config

import io.zensoft.entity.Dictionary
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import javax.persistence.EntityManager

abstract class DictionaryTests(private val clazz: Class<out Enum<*>>, private val tableName: String) : RepositoryTests() {

    @Autowired
    private lateinit var em: EntityManager

    @Test
    fun dictionaryTest() {
        val dbValues = em.createNativeQuery("SELECT * FROM $tableName").resultList
        val enumValues = clazz.enumConstants

        assertThat(dbValues).hasSize(enumValues.size)
        for (value in enumValues) {
            val matched = dbValues.first { (it as Array<*>)[0] == (value as Dictionary).getId() }
            assertThat((matched as Array<*>)[1]).isEqualTo(value.name)
        }
    }

}