package com.sim.ouch

/**
 * com.sim.ouch.Timer and Time-related functions, classes, and variables.
 *
 * @author Jonathan Augustine
 * @since 3.0
 */

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val DD_MM_YYYY_HH_MM_SS: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
val DD_MM_YYYY_HH_MM: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
val WKDAY_MONTH_YEAR: DateTimeFormatter? = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy")
val WKDAY_MONTH_YEAR_TIME: DateTimeFormatter? = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy @ HH:mm")

fun NOW(): OffsetDateTime = OffsetDateTime.now()

/** @return The current local date and time. dd-MM-yyyy HH:mm:ss */
fun NOW_STR(): String = NOW().format(DD_MM_YYYY_HH_MM_SS)

/** @return The current local date and time. dd-MM-yyyy HH-mm-ss */
val NOW_STR_FILE: String get() = NOW_STR().replace(":", "-")

/**
 * Takes in a time length in SECONDS
 * @return W days, X hrs, Y min, Z sec
 */
fun Long.formatTime(): String {
    val d = this / 60 / 60 / 24
    val h = (this / 60 / 60) - 24 * d
    val m = this / 60 - (h * 60) - (d * 60 * 24)
    val s = this - (m * 60) - (h * 60 * 60) - (d * 60 * 60 * 24)
    val list = listOf(d to "day", h to "hr", m to "min", s to "sec")
    val sb = StringBuilder()
    list.forEachIndexed { i, pair ->
        if (pair.first > 0) {
            sb.append("${pair.first} ${pair.second}")
            if (pair.first > 1 && i in (0..1)) sb.append("s")
            sb.append(", ")
        }
    }
    if (sb.length >= 2) sb.setLength(sb.length - 2)
    else { sb.setLength(0); sb.append(0) }
    return sb.toString()
}

/**
 * A com.sim.ouch.Timer object contains a start-time (millisec) that is defined upon creation.
 *
 * @author Jonathan Augustine
 * @since 2.0
 */
class Timer(private var startTime: Long = System.currentTimeMillis()) {

    /** If the timer is running or not */
    private var running: Boolean = true

    /** The time elapsed since the com.sim.ouch.Timer started (-1 the timer is running) */
    private var elapsedTime: Long = -1

    /** @return The time since the com.sim.ouch.Timer started or was stopped */
    fun getElapsedTime(): Long {
        return if (running) {
            System.currentTimeMillis() - startTime
        } else {
            elapsedTime
        }
    }

    companion object {
        fun format(millis: Long): String {
            var seconds = (millis / 1_000).toDouble() // Math.pow(10.0, 9.0)
            var hours = 0
            var min = 0
            if (seconds > 60) {
                var i = 0
                while (i < seconds) {
                    if (i == 60) {
                        min++
                        seconds -= 60.0
                        i = 0
                        if (min == 60) {
                            hours++
                            min -= 60
                        }
                    }
                    i++
                }
            }
            return "$hours:$min:$seconds"
        }
    }

    /** @return The current duration of the timer's run time in HH:MMM:SS */
    override fun toString(): String = format(this.getElapsedTime())

    /** Start the com.sim.ouch.Timer if it is not running */
    fun start() {
        this.running = true
        this.elapsedTime - 1L
    }

    /**
     * Stop the timer (set the end time)
     *
     * @return The formatted duration
     */
    fun stop(): String {
        this.elapsedTime = System.currentTimeMillis()
        this.running = false
        return this.toString()
    }

    /**
     * Resets the starting time and returns the previous duration
     *
     * @return the last duration formatted
     */
    fun reset(): String {
        val lastTime = this.toString()
        this.startTime = System.currentTimeMillis()
        return lastTime
    }
}

/**
 * @param timers Array of Timers
 * @return The average duration of the provided Timers, formatted HH:MMM:SS
 */
fun timerAvg(timers: List<Timer>): String {

    var averageNanoTime: Long = 0

    timers.forEach { averageNanoTime += it.getElapsedTime() }

    averageNanoTime /= timers.size.toLong()

    var seconds = averageNanoTime / Math.pow(10.0, 9.0)
    var hours = 0
    var min = 0
    if (seconds > 60) {
        var i = 0
        while (i < seconds) {
            if (i == 60) {
                min++
                seconds -= 60.0
                i = 0
                if (min == 60) {
                    hours++
                    min -= 60
                }
            }
            i++
        }
    }
    return "$hours:$min:$seconds"
}
