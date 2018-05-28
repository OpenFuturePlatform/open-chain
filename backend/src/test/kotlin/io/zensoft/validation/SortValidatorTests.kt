package io.zensoft.validation

import io.zensoft.domain.base.PageRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SortValidatorTests {

    private val validator: SortValidator = SortValidator()

    @Test
    fun isValidPositiveTest() {
        val pageRequest = PageRequest(sortBy = "XYZ", maySortBy = setOf("XYZ"))

        val value = validator.isValid(pageRequest, null)

        assertThat(value).isTrue()
    }

    @Test
    fun isValidNegativeTest() {
        val pageRequest = PageRequest(sortBy = "XYZ", maySortBy = setOf("ZYX"))

        val value = validator.isValid(pageRequest, null)

        assertThat(value).isFalse()
    }

}