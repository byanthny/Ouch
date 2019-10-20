package com.sim.ouch.extension

import java.io.BufferedWriter
import java.io.File
import java.nio.file.FileAlreadyExistsException

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
