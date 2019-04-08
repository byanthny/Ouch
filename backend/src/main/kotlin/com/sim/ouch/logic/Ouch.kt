package com.sim.ouch.logic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.*
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.sim.ouch.logic.Achievements.*

open class Ouch(var degree: Int = 0) {

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

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonSubTypes(
        Type(value = `Lord Of Oof`::class, name = "lordofoof"),
        Type(value = `Sucks 2 Suck`::class, name = "suckstobeyou"),
        Type(value = `Baby's First Oof`::class, name = "firstoof")
)
sealed class Achievements(val name: String, val description: String) {
    object `Lord Of Oof` : Achievements("Lord of ooF", "You hit the maximum Ouch!")
    object `Sucks 2 Suck` : Achievements("Sucks 2 Suck", "You're halfway to Max Ouch!")
    object `Baby's First Oof` : Achievements("Baby's First ooF", "Aww look at you, so...you.")

    companion object {
        val values = listOf(`Lord Of Oof`, `Sucks 2 Suck`, `Baby's First Oof`)
    }
}
