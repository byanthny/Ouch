/*
 * Copyright Aquatic Mastery Productions (c) 2018.
 */

package com.sim.ouch

val threadName: String get() = Thread.currentThread().name

fun slog(any: Any? = "", inline: Boolean = false) =
        print("${if (inline) "" else "\n"}[$threadName] $any")
fun elog(any: Any? = "", inline: Boolean = false) =
        System.err.print("${if (inline) "" else "\n"}[$threadName] $any")

/** A simple logger that will show it's name each time it prints */
open class Slogger(val name: String = "") {
    fun slog(any: Any = "") = println("[$name] [$threadName] $any")
    fun elog(any: Any = "") = System.err.println("[$name] [$threadName] $any")
}
