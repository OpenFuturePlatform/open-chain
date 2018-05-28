package io.zensoft.util

import io.zensoft.entity.enums.IssueStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DictionaryUtilsTests {

    @Test
    fun valueOfShouldReturnEnum() {
        val status = DictionaryUtils.valueOf(IssueStatus::class.java, IssueStatus.ACCEPTED.getId())
        assertThat(status).isEqualTo(IssueStatus.ACCEPTED)
    }

    @Test(expected = IllegalStateException::class)
    fun valueOfShouldThrowException() {
        DictionaryUtils.valueOf(IssueStatus::class.java, -1)
    }

}