package com.klim.trossage_android.data.local.jwt

import org.junit.Assert.*
import org.junit.Test

class JwtParserTest {

    @Test
    fun `getExpirationTime with valid token returns exp claim`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxNzM1MDY4MDAwfQ.sig"

        val exp = JwtParser.getExpirationTime(token)

        assertEquals(1735068000L, exp)
    }

    @Test
    fun `getExpirationTime with null token returns null`() {
        val exp = JwtParser.getExpirationTime(null)

        assertNull(exp)
    }

    @Test
    fun `getExpirationTime with empty token returns null`() {
        val exp = JwtParser.getExpirationTime("")

        assertNull(exp)
    }

    @Test
    fun `getExpirationTime with malformed token returns null`() {
        val malformedToken = "not.a.valid.jwt.token"

        val exp = JwtParser.getExpirationTime(malformedToken)

        assertNull(exp)
    }

    @Test
    fun `getExpirationTime with token missing exp claim returns null`() {
        val tokenWithoutExp = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.sig"

        val exp = JwtParser.getExpirationTime(tokenWithoutExp)

        assertNull(exp)
    }

    @Test
    fun `isTokenExpiringSoon returns true when token expires in 30 seconds`() {
        val expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEwMDAwMDB9.sig"

        val result = JwtParser.isTokenExpiringSoon(expiredToken, thresholdSeconds = 60)

        assertTrue(result)
    }

    @Test
    fun `isTokenExpiringSoon returns false when token expires in 120 seconds`() {
        val futureToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjk5OTk5OTk5OTl9.sig"

        val result = JwtParser.isTokenExpiringSoon(futureToken, thresholdSeconds = 60)

        assertFalse(result)
    }

    @Test
    fun `isTokenExpiringSoon returns true when token is already expired`() {
        val expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEwMDAwMDB9.sig"

        val result = JwtParser.isTokenExpiringSoon(expiredToken, thresholdSeconds = 60)

        assertTrue(result)
    }

    @Test
    fun `isTokenExpiringSoon returns true for null token`() {
        val result = JwtParser.isTokenExpiringSoon(null, thresholdSeconds = 60)

        assertTrue(result)
    }

    @Test
    fun `isTokenExpiringSoon returns true exactly at threshold`() {
        val expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEwMDAwMDB9.sig"

        val result = JwtParser.isTokenExpiringSoon(expiredToken, thresholdSeconds = 60)

        assertTrue(result)
    }
}
