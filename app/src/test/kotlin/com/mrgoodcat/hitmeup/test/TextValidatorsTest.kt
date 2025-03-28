package com.mrgoodcat.hitmeup.test

import android.content.Context
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.utils.TextUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


@RunWith(MockitoJUnitRunner::class)
class TextValidatorsTest {

    private val FAKE_EMAIL = "nazar1@gmail.com"
    private val FAKE_PASS = "123456"
    private val FAKE_PASS_REPEAT = "123456"

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun `correct email validation`() {
        val mockContext = mock<Context> {}

        val textUtils = TextUtils(mockContext)

        val res = textUtils.validateEmail(FAKE_EMAIL)

        when (res) {
            is TextUtils.ValidateResult.IsNotOk -> assert(false)
            is TextUtils.ValidateResult.IsOk -> assert(true)
        }
    }

    @Test
    fun `correct password validation`() {
        val mockContext = mock<Context> {}

        val textUtils = TextUtils(mockContext)

        val res = textUtils.validatePassword(FAKE_PASS)

        when (res) {
            is TextUtils.ValidateResult.IsNotOk -> assert(false)
            is TextUtils.ValidateResult.IsOk -> assert(true)
        }
    }

    @Test
    fun `correct password repeat validation`() {
        val mockContext = mock<Context> {}

        val textUtils = TextUtils(mockContext)

        val res = textUtils.validateRepeatedPassword(FAKE_PASS, FAKE_PASS_REPEAT)

        when (res) {
            is TextUtils.ValidateResult.IsNotOk -> assert(false)
            is TextUtils.ValidateResult.IsOk -> assert(true)
        }
    }

    @Test
    fun `incorrect email validation`() {
        val mockContext = mock<Context> {
            on { getString(R.string.email_is_empty) } doReturn "Email is empty"
        }

        val textUtils = TextUtils(mockContext)

        val res = textUtils.validateEmail("")

        when (res) {
            is TextUtils.ValidateResult.IsNotOk -> assert(true)
            is TextUtils.ValidateResult.IsOk -> assert(false)
        }
    }

    @Test
    fun `incorrect password validation`() {
        val mockContext = mock<Context> {
            on { getString(R.string.password_is_empty) } doReturn "Password is empty"
        }

        val textUtils = TextUtils(mockContext)

        val res = textUtils.validatePassword("")

        when (res) {
            is TextUtils.ValidateResult.IsNotOk -> assert(true)
            is TextUtils.ValidateResult.IsOk -> assert(false)
        }
    }

    @Test
    fun `incorrect password repeat validation`() {
        val mockContext = mock<Context> {
            on { getString(R.string.password_repeate_error) } doReturn "Password repeat error"
        }

        val textUtils = TextUtils(mockContext)

        val res = textUtils.validateRepeatedPassword(FAKE_PASS_REPEAT, "")

        when (res) {
            is TextUtils.ValidateResult.IsNotOk -> assert(true)
            is TextUtils.ValidateResult.IsOk -> assert(false)
        }
    }
}