package me.uport.sdk.demoapp

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test

class DemoTests {

    @Test
    fun format_exception_returns_non_null_response() {
        assertThat(formatException(Exception())).isNotNull()
    }

    @Test
    fun format_exception_returns_null_response() {
        assertThat(formatException(null)).isNull()
    }
}