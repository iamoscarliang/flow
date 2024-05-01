package com.oscarliang.flow.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class RateLimiterTest {

    private lateinit var rateLimiter: RateLimiter<String>

    @Before
    fun init() {
        rateLimiter = RateLimiter(1, TimeUnit.SECONDS)
    }

    @Test
    fun testFetchWhenTimeout() = runTest {
        val key = "foo"
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
        advanceTimeBy(500)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), false)
        advanceTimeBy(500)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), false)
    }

    @Test
    fun testReset() = runTest {
        val key = "foo"
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), false)
        rateLimiter.reset(key)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
    }

    @Test
    fun testFetchAndReset() = runTest {
        val key = "foo"
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
        advanceTimeBy(1000)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), false)
        rateLimiter.reset(key)
        assertEquals(rateLimiter.shouldFetch(key, currentTime), true)
    }

}