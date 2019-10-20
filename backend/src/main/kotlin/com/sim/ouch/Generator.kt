package com.sim.ouch

import com.sim.ouch.extension.RAND
import com.sim.ouch.extension.alpha
import com.sim.ouch.extension.alphaDigi
import kotlin.streams.asSequence

/**
 * An random ID generator. 0-9, A-Z (caps)
 *
 * @property leng the length of an ID
 * @property prefix
 *
 * @author Jonathan Augustine
 * @since 2.0
 */
open class IDGenerator(var leng: Long = 10L, val prefix: String = "") {
    val next: String
        get() = prefix + RAND.ints(leng, 0, alphaDigi.length)
            .asSequence()
            .map(alphaDigi::get)
            .joinToString("")

    companion object {
        private object Default : IDGenerator()
        /** Get the next default ID. */
        val nextDefault get() = Default.next
    }
}

/**
 * TODO name generator
 *
 * @property lengthRange
 */
class NameGenerator(val lengthRange: IntRange = 2..7) {
    val next get() = RAND.ints(lengthRange.random().toLong(), 0, alpha.length)
        .asSequence().map(alpha.toLowerCase()::get).joinToString("")

    companion object {
        private val Default = NameGenerator()
        val nextDefault get() = Default.next
    }
}
