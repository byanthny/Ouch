package com.sim.ouch

import com.sim.ouch.web.server

class OuchData(val version: String, val uri: String, vararg val authors: String)

val OUCH = OuchData("0.0.0", "https://anthnyd.github.io/Ouch/",
    "Jonathan Augustine", "Anthony Das")

fun main() = server.unit
