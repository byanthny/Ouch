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

enum class Action(val callform: String, val description: String) {
    BEAN("-bean", "get beaned"),
    THEME("-theme", "darkmode best mode"),
    EXIT("-exit", "disconnect from the Existence")
}
