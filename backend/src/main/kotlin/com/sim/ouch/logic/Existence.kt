package com.sim.ouch.logic

import com.sim.ouch.*
import com.sim.ouch.extension.nowISO
import com.sim.ouch.web.Chat
import com.soywiz.klock.DateTime
import kotlinx.serialization.SerialName

/**
 * The simulation.
 *
 * @property capacity The maximum number of entities allowed in this existence.
 * `-1` if unlimited.
 * @property public Whether this existence should be publicly available.
 */
class Existence(
    val name: Name,
    var capacity: Int = -1,
    var public: Boolean = false
) {

    @SerialName("id")
    val id: EC = IDGenerator.nextDefault
    val init = DateTime.now()

    /** The first [Quiddity] to enter the [Existence]. */
    val quidities: MutableMap<String, Quiddity> = mutableMapOf()
    val infraQuidities: MutableMap<String, InfraQuidity> = mutableMapOf()

    val size: Int get() = quidities.size + infraQuidities.size
    val full: Boolean get() = qSize == capacity
    val qSize: Int get() = quidities.size
    val sessionCount: Int get() = sessionTokens.size

     val sessionTokens: MutableList<Token> = mutableListOf()

    var dormantSince: String? = null

    var status: Status = Status.DRY
        set(value) {
            dormantSince = when (value) {
                Status.DORMANT -> DateTime.now().nowISO
                else -> null
            }
            field = value
        }
        get() {
            if (sessionTokens.isEmpty()) field = Status.DORMANT
            return field
        }

    val chat: Chat = Chat()

    /** Generate a new [Quiddity] and add it to the [Existence]. */
    fun generateQuidity(name: String): Quiddity = Quiddity(name).also { enter(it) }

    /** Add an [entity] to the [Existence]. */
    fun enter(entity: Entity) {
        when (entity) {
            is Quiddity -> quidities[entity.id] = entity
            is InfraQuidity -> infraQuidities[entity.id] = entity
        }
    }

    operator fun get(id: ID) = quidities[id] ?: infraQuidities[id]
    fun qOf(id: QC) = quidities[id]
    fun qOfName(name: String) =
        quidities.values.firstOrNull { it.name.equals(name, true) }

    fun infraOf(id: ID) = infraQuidities[id]

    /** Add a new [Session token][Token]. */
    fun addSession(token: Token) = sessionTokens.add(token)

    /** Remove a [Session token][Token]. */
    fun removeSession(token: Token) = sessionTokens.remove(token)

    enum class Status { DORMANT, WET, DRY }

    companion object {
        val newDefault get() = Existence(NameGenerator.nextDefault)
        val newPublic get() = Existence("Public", 100, true)
    }
}

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
