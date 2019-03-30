package com.sim.ouch

abstract class Entity {

    open val id: String = DEFAULT_ID_GEN.next()

    companion object {
        val DEFAULT_ID_GEN = IDGenerator(10)
    }
}

/** The essence of... */
abstract class Quidity : Entity() {
    open var ouch: Boolean = false
        set(value) { field = if (!field) value else field }

    class Ouch(var degree: Int) {

        operator fun inc(): Ouch {
            check((degree + 1) <= OUCH_RANGE.endInclusive) { "Ouch at max." }
            return this.apply { degree++ }
        }

        operator fun dec(): Ouch {
            check((degree - 1) <= OUCH_RANGE.start) { "Ouch at min." }
            return this.apply { degree-- }
        }

        companion object {
            var OUCH_RANGE = 0..100 // TODO Test range
        }
    }
}

abstract class InfraQuidity : Entity()
