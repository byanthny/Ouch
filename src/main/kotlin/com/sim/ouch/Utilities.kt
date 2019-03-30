package com.sim.ouch

import java.util.*
import kotlin.streams.asSequence

val RAND = Random(420_69_98_4829 / (NOW().minute + 1))

const val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val digi  = "0123456789"
const val alphaDigi = alpha + "123456789" //Exclued Zero

/**
 * An random ID generator. 0-9, A-Z (caps)
 *
 * @param idLeng the length of an ID
 *
 * @author Jonathan Augustine
 * @since 2.0
 */
open class IDGenerator(var idLeng: Long = 7L, val prefix: String = "") {
    fun next() : String {
        return prefix + RAND.ints(idLeng, 0, alphaDigi.length)
            .asSequence().map(alphaDigi::get).joinToString("")
    }
}
