package com.sim.ouch.logic

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.sim.ouch.logic.Achievements.*

/*
 * File Contents:
 * private:
 *      loadKeyWords(): Map<Regex, Double>
 *          - Loads Ouch class keywords from the keywords.oof file
 *
 */

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
        val keywords: Map<Regex, Double> by lazy { loadKeyWords() }
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

/**
 * Recursively load the keywords from the `keywords.oof` file in `src/main/resources/`.
 *
 * The file contents are in `Regoof` (Regex for oof) which is a simplified
 * take on Regex. The `Regoof` needs to be parsed into a normal [Regex].
 *
 * @return A [Map] of Keyword [Regex] -> [Double] values
 */
private fun loadKeyWords(): Map<Regex, Double> {
    val map = mutableMapOf<Regex, Double>()
    fun parse(string: String, value: Double? = null) {
        // Split by line-final (;)
        string.split(';').filterNot(String::isBlank).apply {
            // If this step produced any values, reparse
            if (size > 1) return forEach { parse(it) }
            // else continue to the next check
        }
        // Split digit and save to reparse
        string.split('|').filterNot(String::isBlank).apply {
            // This split should turn this:
            // "word:some phrase:another one|20"
            // into this: ["word:some phrase:another one", "20"]
            if (size > 2) throw IllegalStateException(
                "invalid parse state. more than 1 digit delimiter."
            )
            else if (size == 2) {
                // Parse the value as a Long to allow flexibility
                // Then convert it to a milliOof double
                // then parse again
                return this[1].toLongOrNull()?.div(1_000.0)
                    ?.let { v -> parse(this[0], v) }
                    ?: throw IllegalStateException(
                        "invalid parse state. value must be an long or integer"
                    )
            }
        }
        // Split by word-linker (:)
        string.split(':').filterNot(String::isBlank).apply {
            // results in a list of words that need to be parsed one last time
            if (size > 1) return forEach { parse(it, value) }
        }
        // Parse simplified regex
        if (value == null) throw IllegalStateException()
        string.replace(Regex("\\s+"), "\\s+")
            .let {
                it.replace(Regex("\\((?s).*\\)")) { res ->
                    res.value.replace("&", "|")
                }
            }.let { Regex("(?i)$it") }.also { map[it] = value }
    }
    Ouch::class.java.getResource("/keywords.oof")?.openStream()
        ?.use { it.reader().readLines() }
        ?.filterNot { it.trim().startsWith("//") } // Filter comments
        ?.joinToString("")
        ?.let { parse(it) }
        ?: throw Exception("Failed to load resource 'keywords.oof'")

    return map
}

