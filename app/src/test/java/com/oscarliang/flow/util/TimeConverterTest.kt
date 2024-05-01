package com.oscarliang.flow.util

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TimeConverterTest {

    private lateinit var timeConvert: TimeConverter

    @Before
    fun init() {
        timeConvert = TimeConverter
    }

    @Test
    fun testGetTimePass() {
        val now = 1714532577483
        val time = timeConvert.getTimePassBy(24, now)
        assertEquals(time, "2024-04-30")
    }

}