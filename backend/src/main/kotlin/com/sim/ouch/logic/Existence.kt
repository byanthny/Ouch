package com.sim.ouch.logic

import com.sim.ouch.IDGenerator

/** The simulation. */
sealed class Existence {
    open val id = EXISTENCE_ID_GEN.next()
    var status: Status = Status.DRY

    abstract val name: String
    abstract val capacity: Long
    /** The first [Quidity] to enter the [Existence]. */
    abstract val initialQuidity: Quidity
    open val quidities: MutableMap<String, Quidity> = mutableMapOf()
    open val infraQuidities: MutableMap<String, InfraQuidity> = mutableMapOf()

    /** Add an [entity] to the [Existence]. */
    open fun enter(entity: Entity) {
        when (entity) {
            is Quidity -> quidities[entity.id] = entity
            is InfraQuidity -> infraQuidities[entity.id] = entity
        }
    }

    operator fun get(id: String) = quidities[id] ?: infraQuidities[id]

    enum class Status { OFF, WET, DRY }

    companion object {
        val EXISTENCE_ID_GEN = IDGenerator(10)
    }
}

class DefaultExistence(
        override val name: String,
        override val capacity: Long = -1,
        override val initialQuidity: Quidity
) : Existence()

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
