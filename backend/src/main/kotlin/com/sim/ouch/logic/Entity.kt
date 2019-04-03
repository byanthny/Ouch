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

    enum class Action(val prettyName: String, val description: String) {
        TEST("Test Action", "Description"),
        TEST_2("Test Action 2", "Description 2"),
        TEST_3("Test Action 3", "Description 3")
    }

}

abstract class InfraQuidity : Entity()
