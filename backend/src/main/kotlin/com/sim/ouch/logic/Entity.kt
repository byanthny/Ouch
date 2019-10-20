package com.sim.ouch.logic

import com.sim.ouch.IDGenerator
import com.sim.ouch.QC
import kotlinx.serialization.Serializable

@Serializable
sealed class Entity {
    @Serializable
    open val id: QC = IDGenerator.nextDefault
}

/** The essence of... */
@Serializable
open class Quiddity(open var name: String) : Entity() {
    /** The gradial state of Ouchies. */
    open var ouch: Ouch = Ouch()

    open fun love() = ouch--
}

@Serializable
abstract class InfraQuidity : Entity()
