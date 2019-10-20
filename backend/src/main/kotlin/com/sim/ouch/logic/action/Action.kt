package com.sim.ouch.logic.action

import kotlinx.serialization.Serializable

@Serializable
sealed class Action(val callform: String, val description: String) {

    companion object {
        val values =
            Action::class.sealedSubclasses.mapNotNull { it.objectInstance }
        val map = values.associateBy { it.callform }
    }

    object BEAN : Action("-bean", "get beaned")
    object MUSIC : Action("-music", "feel the ouchie vibes")
    object THEME : Action("-theme", "darkmode best mode")
    object EXIT : Action("-exit", "disconnect from the Existence")

    override fun equals(other: Any?): Boolean =
        (other is Action && other.callform == this.callform) ||
            (other is String && other == this.callform)
}

val String.asAction get() = Action.map[this]
