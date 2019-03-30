package com.sim.ouch.web

import io.javalin.Javalin

val DAO: Dao by lazy { Dao() }
lateinit var javalin: Javalin


fun main() {

    javalin = Javalin.create().apply {

        before("/login") { context ->

        }

    }
}
