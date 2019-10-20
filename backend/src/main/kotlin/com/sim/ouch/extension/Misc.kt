package com.sim.ouch.extension

import com.soywiz.klock.DateTime
import io.javalin.Javalin
import java.util.*

val RAND by lazy { Random(420_69_98_4829 / (DateTime.now().seconds)) }

val Any.unit get() = Unit

fun secret(javalin: Javalin) {
    javalin.get("/ph") { it.redirect("https://pornhub.com/random") }
}
