package com.sim.ouch.logic

import com.sim.ouch.DefaultNameGenerator
import com.sim.ouch.IDGenerator
import com.sim.ouch.web.Chat
import io.javalin.websocket.WsSession

/** The simulation. */
sealed class Existence {

    @Transient val sessions = mutableMapOf<String, WsSession>()

    open val id = EXISTENCE_ID_GEN.next()
    var status: Status = Status.DRY

    abstract val name: String
    abstract val capacity: Long
    abstract val chat: Chat
    /** The first [Quidity] to enter the [Existence]. */
    abstract val initialQuidity: Quidity
    open val quidities: MutableMap<String, Quidity> = mutableMapOf()
    open val infraQuidities: MutableMap<String, InfraQuidity> = mutableMapOf()

    abstract fun generateQuidity(name: String): Quidity

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
        override val initialQuidity: Quidity,
        override val capacity: Long = -1,
        override val name: String  = DefaultNameGenerator.next()
) : Existence() {

    override val chat: Chat = Chat(this)

    init {
        quidities[initialQuidity.id] = initialQuidity
    }

    override fun generateQuidity(name: String) = Quidity(name)
}

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
