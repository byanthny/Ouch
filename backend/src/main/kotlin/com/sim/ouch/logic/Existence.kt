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
sealed class Existence(
    val name: Name,
    var capacity: Long = 1,
    var public: Boolean = false
) {

    @BsonId open val _id: EC = EXISTENCE_ID_GEN.next()
    open val init = NOW()

    /** The first [Quiddity] to enter the [Existence]. */
    open val quidities: MutableMap<String, Quiddity> = mutableMapOf()
    open val infraQuidities: MutableMap<String, InfraQuidity> = mutableMapOf()

    val size: Int get() = quidities.size + infraQuidities.size
    val qSize: Int get() = quidities.size
    val sessionCount: Int get() = sessionTokens.size

    open val sessionTokens: MutableList<Token> = mutableListOf()
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

    val chat: Chat = Chat()

    /** Generate a new [Quiddity] and add it to the [Existence]. */
    abstract fun generateQuidity(name: String): Quiddity

    /** Add an [entity] to the [Existence]. */
    open fun enter(entity: Entity) {
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
        val EXISTENCE_ID_GEN = IDGenerator(10)
    }
}

open class DefaultExistence(
    name: String  = DefaultNameGenerator.next(),
    capacity: Long = -1
) : Existence(name, capacity) {
    override fun generateQuidity(name: String) = Quiddity(name)
        .also { enter(it) }
}

/** A public [Existence]. */
class PublicExistence : DefaultExistence("Public", 1_000) {
    //override val capacity: Long = 1_000
    init {
        public = true
    }
}

/** That which possess the [Existence]. */
interface Simulator {
    val name: String
}
