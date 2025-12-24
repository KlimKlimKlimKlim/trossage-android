package com.klim.trossage_android.data.remote.network

import com.google.gson.Gson
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import io.mockk.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthHeaderInterceptorTest {

    private lateinit var interceptor: AuthHeaderInterceptor
    private lateinit var authPrefs: AuthPreferences
    private lateinit var chain: Interceptor.Chain
    private val baseUrl = "https://test.api/"
    private val gson = Gson()

    @Before
    fun setup() {
        authPrefs = mockk(relaxed = true)
        interceptor = AuthHeaderInterceptor(authPrefs, baseUrl, gson)
        chain = mockk()
    }

    @Test
    fun `adds access token to request header`() {
        val validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjk5OTk5OTk5OTl9.sig"
        every { authPrefs.getAccessToken() } returns validToken

        val originalRequest = Request.Builder()
            .url("https://test.api/users/me")
            .build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockk(relaxed = true)

        interceptor.intercept(chain)

        verify {
            chain.proceed(match { request ->
                request.header("Authorization") == "Bearer $validToken"
            })
        }
    }

    @Test
    fun `does not add token when token is null`() {
        every { authPrefs.getAccessToken() } returns null

        val originalRequest = Request.Builder()
            .url("https://test.api/users/me")
            .build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockk(relaxed = true)

        interceptor.intercept(chain)

        verify {
            chain.proceed(match { request ->
                request.header("Authorization") == null
            })
        }
    }

    @Test
    fun `uses refresh token for REFRESH auth type requests`() {
        val refreshToken = "refresh_token_123"
        every { authPrefs.getRefreshToken() } returns refreshToken
        every { authPrefs.getAccessToken() } returns "access_token"

        val originalRequest = Request.Builder()
            .url("https://test.api/auth/refresh")
            .header(AuthType.HEADER, AuthType.REFRESH)
            .post("".toRequestBody(null))
            .build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockk(relaxed = true)

        interceptor.intercept(chain)

        verify {
            chain.proceed(match { request ->
                request.header("Authorization") == "Bearer $refreshToken" &&
                        request.header(AuthType.HEADER) == null
            })
        }
    }

    @Test
    fun `does not perform proactive refresh when token is valid for more than 60 seconds`() {
        val validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjk5OTk5OTk5OTl9.sig"
        every { authPrefs.getAccessToken() } returns validToken

        val originalRequest = Request.Builder()
            .url("https://test.api/users/me")
            .build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockk(relaxed = true)

        interceptor.intercept(chain)

        verify(exactly = 0) { authPrefs.getRefreshToken() }
        verify(exactly = 2) { authPrefs.getAccessToken() }
    }

}
