package com.sim.ouch

abstract class Entity {
    open val id: String = DEFAULT_ID_GEN.next()
    companion object {
        val DEFAULT_ID_GEN = IDGenerator(10)
    }
}

/** The essence of... */
abstract class Quidity : Entity() {
    /** The gradial state of Ouchies. */
    open var ouch: Ouch = Ouch()
    open fun love() = ouch--
}

abstract class InfraQuidity : Entity()

open class Ouch(var degree: Int = 0) {

    operator fun inc(): Ouch {
        check((degree + 1) <= OUCH_RANGE.endInclusive) { "Ouch at max." }
        return this.apply { degree++ }
    }

    operator fun dec(): Ouch {
        check((degree - 1) <= OUCH_RANGE.start) { "Ouch at min." }
        return this.apply { degree-- }
    }

    sealed class Achievements(val name: String, val description: String) {
        class MaxOuch : Achievements("OOF", "You hit the maximum Ouch!")
        class SucksToBeYou : Achievements("Sucks_2_Suck", "You're halfway to Max Ouch!")
        class FirstOof : Achievements("Baby's First Off", "Aww look at you, so...you.")
    }

    companion object {
        var OUCH_RANGE = 0..100 // TODO Test range
    }
}
