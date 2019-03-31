package com.sim.ouch

import java.io.BufferedWriter
import java.io.File
import java.nio.file.FileAlreadyExistsException
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

val DefaultNameGenerator = NameGenerator()

// TODO
class NameGenerator(val lengthRange: IntRange = 2..7) {
    fun next(): String {
        return RAND.ints(lengthRange.random().toLong(), 0, alpha.length)
            .asSequence().map(alpha.toLowerCase()::get).joinToString("")
    }
}

/**
 * Print the List to file, each index its own line.
 *
 * @param name The name of the file.
 * @return The [File] made or `null` if unable to create.
 * @throws FileAlreadyExistsException
 */
@Throws(FileAlreadyExistsException::class)
fun List<Any>.toFile(name: String = "file") : File {
    fun BufferedWriter.writeLn(line: Any) {
        this.write(line.toString())
        this.newLine()
    }
    val file = File(name)
    //Leave if the file already exists
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

/** Remove and return the last entry of the [list][MutableList]. `null` if empty. */
internal fun <E> MutableList<E>.removeLastOrNull() = if (isEmpty()) null else removeAt(this.size - 1)

val Any.unit get() = Unit
