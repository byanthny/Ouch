package com.sim.ouch.extension

const val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val digi = "0123456789"
/** Excludes Zero */
const val alphaDigi = alpha + "123456789"

fun String.allMatches(vararg patterns: String) =
    allMatches(*patterns.map(String::toRegex).toTypedArray())

fun String.allMatches(vararg regex: Regex) = allMatches(regex.toList())

fun String.allMatches(regex: Iterable<Regex>) = regex.filter { matches(it) }
