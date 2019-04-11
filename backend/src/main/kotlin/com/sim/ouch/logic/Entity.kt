package com.sim.ouch.logic

import com.sim.ouch.IDGenerator
import com.sim.ouch.web.QC

sealed class Entity {
    open val id: QC = DEFAULT_ID_GEN.next()
    companion object {
        val DEFAULT_ID_GEN = IDGenerator(10)
    }
}

/** The essence of... */
open class Quiddity(open var name: String) : Entity() {
    /** The gradial state of Ouchies. */
    open var ouch: Ouch = Ouch()
    open fun love() = ouch--
}

abstract class InfraQuidity : Entity()

sealed class Action(val callform: String, val description: String) {

    companion object {
        val values = Action::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    object BEAN : Action("-bean", "get beaned")
    object MUSIC : Action("-music", "feel the ouchie vibes")
    object THEME : Action("-theme", "darkmode best mode")
    object EXIT : Action("-exit", "disconnect from the Existence")
}
