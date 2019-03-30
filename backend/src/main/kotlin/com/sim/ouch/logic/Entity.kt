package com.sim.ouch.logic

import com.sim.ouch.IDGenerator

sealed class Entity {
    open val id: String = DEFAULT_ID_GEN.next()
    companion object {
        val DEFAULT_ID_GEN = IDGenerator(10)
    }
}

/** The essence of... */
open class Quidity : Entity() {
    /** The gradial state of Ouchies. */
    open var ouch: Ouch = Ouch()
    open fun love() = ouch--
}

abstract class InfraQuidity : Entity()
