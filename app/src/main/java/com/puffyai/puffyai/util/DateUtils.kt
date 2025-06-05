package com.puffyai.puffyai.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private const val DATE_FORMAT = "yyyyMMdd"
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    /**
     * Returns the current date as a string in "yyyyMMdd" format.
     */
    fun getCurrentDateString(): String {
        return dateFormat.format(Date())
    }

    /**
     * Calculates the delay in milliseconds until the next midnight in the specified time zone.
     * This is useful for scheduling daily tasks.
     *
     * @param timeZone The time zone to consider for midnight (e.g., "Europe/Madrid").
     * @return The delay in milliseconds until the next midnight.
     */
    fun calculateDelayToMidnight(timeZone: String = "Europe/Madrid"): Long {
        val now = Date()
        val calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(timeZone), Locale.getDefault())
        calendar.time = now
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 1) // 1 second past midnight
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        return calendar.timeInMillis - now.time
    }
}