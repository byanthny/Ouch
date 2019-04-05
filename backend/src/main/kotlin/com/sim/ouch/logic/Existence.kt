package com.sim.ouch.logic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.sim.ouch.DefaultNameGenerator
import com.sim.ouch.IDGenerator
import com.sim.ouch.NOW
import com.sim.ouch.web.*
import org.bson.codecs.pojo.annotations.BsonId
import java.time.OffsetDateTime

/** The simulation. */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes(
    Type(value = DefaultExistence::class, name = "default"),
    Type(value = PublicExistence::class, name = "public")
)
sealed class Existence {

    @BsonId open val _id: EC = EXISTENCE_ID_GEN.next()
    open val init = NOW()
    open var dormantSince: OffsetDateTime? = null
    var status: Status = Status.DRY
        set(value) {
            when (value) {
                Status.DORMANT -> dormantSince = NOW()
                else -> dormantSince = null
            }
            field = value
        }
    get() {
        if (sessionTokens.isEmpty()) field = Status.DORMANT
        return field
    }

    abstract val name: String
    abstract val capacity: Long
    /** The first [Quiddity] to enter the [Existence]. */
    abstract val initialQuiddity: Quiddity
    open val quidities: MutableMap<String, Quiddity> = mutableMapOf()
    open val infraQuidities: MutableMap<String, InfraQuidity> = mutableMapOf()
    open val sessionTokens: MutableList<Token> = mutableListOf()
    val chat: Chat = Chat()
    var public = false

    abstract fun generateQuidity(name: String): Quiddity

    /** Add an [entity] to the [Existence]. */
    open fun enter(entity: Entity) {
        when (entity) {
            is Quiddity -> quidities[entity.id] = entity
            is InfraQuidity -> infraQuidities[entity.id] = entity
        }
    }

    operator fun get(id: String) = quidities[id] ?: infraQuidities[id]

    /** Add a new [Session token][Token]. */
    fun addSession(token: Token) = sessionTokens.add(token)
    /** Remove a [Session token][Token]. */
    fun removeSession(token: Token) = sessionTokens.remove(token)

    enum class Status { DORMANT, WET, DRY }

    companion object {
        val EXISTENCE_ID_GEN = IDGenerator(10)
    }
}

/** Broadcast the [packet] to all connected sessions. */
fun Existence.broadcast(packet: Packet) {
    val s = packet.pack()
    sessionTokens.forEach { DAO.getSession(it)?.send(s) }
}

open class DefaultExistence(
    override val initialQuiddity: Quiddity,
    override val capacity: Long = -1,
    override val name: String  = DefaultNameGenerator.next()
) : Existence() {
    override fun generateQuidity(name: String) = Quiddity(name)
}

/** A public [Existence]. */
class PublicExistence : DefaultExistence(Quiddity("null"), name = "-TEST") {
    init {
        public = true
    }
}

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
