package com.sim.ouch.logic

import com.sim.ouch.IDGenerator
import com.sim.ouch.QC
import kotlinx.serialization.Serializable

sealed class Entity {
    @Serializable open val id: QC = IDGenerator.nextDefault
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
