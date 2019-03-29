package com.sim.ouch

/** The essence of... */
interface Quidity {
    val id: String
}

/** The simulation. */
interface Existence {
    enum class Status { WET, DRY }
    val name: String
    val capacity: Long
}

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
