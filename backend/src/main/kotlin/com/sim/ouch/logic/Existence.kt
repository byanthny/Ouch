package com.sim.ouch.logic

import com.sim.ouch.IDGenerator

/** The simulation. */
abstract class Existence {
    open val id = EXISTENCE_ID_GEN.next()
    var status: Status = Status.DRY

    abstract val name: String
    abstract val capacity: Long
    /** The first [Quidity] to enter the [Existence]. */
    abstract val initialQuidity: Quidity
    abstract val quidities: MutableMap<String, Quidity>
    abstract val infraQuidities: MutableMap<String, InfraQuidity>

    /** Add an [entity] to the [Existence]. */
    abstract fun enter(entity: Entity)

    operator fun get(id: String) = quidities[id] ?: infraQuidities[id]

    enum class Status { OFF, WET, DRY }

    companion object {
        val EXISTENCE_ID_GEN = IDGenerator(10)
    }
}

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
