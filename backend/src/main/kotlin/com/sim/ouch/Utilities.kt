package com.sim.ouch

import io.javalin.Javalin
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.util.*
import kotlin.streams.asSequence

// Generators

/**
 * An random ID generator. 0-9, A-Z (caps)
 *
 * @param idLeng the length of an ID
 *
 * @author Jonathan Augustine
 * @since 2.0
 */
open class IDGenerator(var idLeng: Long = 7L, val prefix: String = "") {
    fun next(): String {
        return prefix + RAND.ints(idLeng, 0, alphaDigi.length)
            .asSequence().map(alphaDigi::get).joinToString("")
    }
}

val DefaultNameGenerator = NameGenerator()

// TODO
class NameGenerator(val lengthRange: IntRange = 2..7) {
    fun next() = RAND.ints(lengthRange.random().toLong(), 0, alpha.length)
        .asSequence().map(alpha.toLowerCase()::get).joinToString("")
}

// File

// Collections

/**
 * Print the List to file, each index its own line.
 *
 * @param name The name of the file.
 * @return The [File] made or `null` if unable to create.
 * @throws FileAlreadyExistsException
 */
@Throws(FileAlreadyExistsException::class)
fun List<Any>.toFile(name: String = "file"): File {
    fun BufferedWriter.writeLn(line: Any) {
        this.write(line.toString())
        this.newLine()
    }
    val file = File(name)
    // Leave if the file already exists
    if (file.exists()) {
        throw FileAlreadyExistsException("File '$name' already exists.")
    } else {
        file.createNewFile()
        val bw = file.bufferedWriter()
        this.forEach { bw.writeLn(it) }
        bw.close()
    }
    return file
}

fun <T> Collection<T>.avgBy(f: (T) -> Int) =
    if (isEmpty()) 0.0 else sumBy(f) / size.toDouble()

/** Remove and return the last entry of the [list][MutableList]. `null` if empty. */
fun <E> MutableList<E>.removeLastOrNull() =
    if (isEmpty()) null else removeAt(this.size - 1)

// String & Regex

fun String.allMatches(vararg patterns: String) =
    allMatches(*patterns.map(String::toRegex).toTypedArray())

fun String.allMatches(vararg regex: Regex) = allMatches(regex.toList())

fun String.allMatches(regex: Iterable<Regex>) = regex.filter { matches(it) }

// Coroutine

val handler = CoroutineExceptionHandler { _, thr ->
    thr.cause?.printStackTrace() ?: thr.printStackTrace()
}

fun launch(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(
    handler, block = block)

// Misc

val RAND = Random(420_69_98_4829 / (NOW().minute + 1))

const val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val digi = "0123456789"
/** Excludes Zero */
const val alphaDigi = alpha + "123456789"

inline fun <T> T.given(condition: Boolean, action: (T) -> Any) {
    if (condition) action(this)
}

inline fun <T> T.given(condition: (T) -> Boolean, action: (T) -> Any) {
    if (condition(this)) action(this)
}

inline fun Boolean.`if`(action: () -> Any) {
    if (this) action()
}

inline fun Boolean.ifNot(action: () -> Any) {
    if (!this) action()
}

val Any.nil get() = null
val Any.unit get() = Unit

fun secret(javalin: Javalin) {
    javalin.get("/ph") { it.redirect("https://pornhub.com/random") }
}
