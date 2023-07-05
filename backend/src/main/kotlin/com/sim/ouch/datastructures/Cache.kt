package com.sim.ouch.datastructures

import com.sim.ouch.extension.removeLastOrNull
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A Caching Interface which presents a framework for abstracting away from a [Map], allowing for more detailed
 * internal control over caching behavior.
 */
typealias Kache<K, V> = MutableMap<K, V>

/** A [Kache] implementation which prioritizes the usage time of entries during size maintenance. */
abstract class UsagePriorityKache<K, V> : Kache<K, V> {
    /** An immutable clone of the cache's current state. */
    val image get() = toMap()
    /** An internal list used to track the usage of entries. */
    protected abstract val usageRanks: MutableList<K>

    /** The entry to remove when the list has reached capacity and needs to insert a new value */
    abstract val evictTarget: K?

    companion object {
        const val DEFAULT_MIN = 100
        const val DEFAULT_MAX = 10_000
    }
}

/**
 * An Implementation of [UsagePriorityKache] which removes [trashSize]-number of the least recently used entry
 * when space is needed. Stores by [key][K]/[value][V] pairs, and takes the [minimum size][minSize] and
 * [maximum size][maxSize] of the cache as constructor parameters.
 *
 * See [LRU](https://en.wikipedia.org/wiki/Cache_replacement_policies#LRU)
 *
 * @property minSize The minimum size the [LruKache] will self reduce to during downsizing.
 * *This takes priority over [trashSize]*.
 * @property maxSize the maximum number of entries allowed before new entries will cause downsizing.
 * @property trashSize The number of elements to remove during a downsizing.
 */
class LruKache<K, V>(
    val maxSize: Int = DEFAULT_MAX,
    val minSize: Int = DEFAULT_MIN,
    val trashSize: Int = DEFAULT_TRASH_SIZE
) : UsagePriorityKache<K, V>() {
    private val map = ConcurrentHashMap<K, V>()
    override val size get() = map.size
    override val entries = map.entries
    override val keys = map.keys
    override val values = map.values
    /** 0 == greatest usage or most recent */
    override val usageRanks = mutableListOf<K>()
    override val evictTarget get() = usageRanks.removeLastOrNull()

    init {
        if (trashSize < 1) throw IllegalArgumentException("LRU TrashSize must be greater than 0.")
    }

    /**
     * Set a [Token][K]-[Value][V] pair in cache. If the cache is at [maxSize],
     * remove [trashSize]-number [entries][evictTarget] then add the new entry.
     * @return the [value][V] previously at [key]
     */
    override fun put(key: K, value: V): V? {
        if (key !in this) {
            // Add key to usage ranks
            usageRanks += key
            // Downsize on max-size
            if (size == maxSize) {
                var i = 0
                while (size > minSize && i++ < trashSize) evictTarget?.also { this.remove(it) } ?: break
            }
        }
        return map.put(key, value)
    }

    /**
     * Retrieve a [value][V] and set to MOST recently used
     *
     * @return the [value][V] at [key] or null
     */
    override fun get(key: K): V? = map[key]?.also {
        usageRanks -= key
        usageRanks.add(0, key)
    }

    operator fun minusAssign(key: K) = map.minusAssign(key).also { usageRanks.remove(key) }

    override fun containsKey(key: K) = map.containsKey(key)

    override fun containsValue(value: V) = map.containsValue(value)

    override fun isEmpty() = map.isEmpty()

    override fun clear() {
        map.clear()
        usageRanks.clear()
    }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from)
        usageRanks += from.keys
    }

    override fun remove(key: K): V? = map.remove(key)

    companion object {
        const val DEFAULT_TRASH_SIZE = 1
    }
}

/**
 * @property trashAction
 */
class ExpiringKache<K, V>(
    val maxSize: Int = DEFAULT_MAX,
    val minSize: Int = DEFAULT_MIN,
    val trashSize: Int = DEFAULT_TRASH_SIZE,
    val trashAction: suspend (Map<K, V?>) -> Unit
) : UsagePriorityKache<K, V>() {
    private val map = ConcurrentHashMap<K, V>()
    override val size get() = map.size
    override val entries get() = map.entries
    override val keys get() = map.keys
    override val values get() = map.values
    override val usageRanks = mutableListOf<K>()
    override val evictTarget get() = usageRanks.removeAt(0)

    init {
        if (trashSize < 1) throw IllegalArgumentException(
            "TrashSize must be greater than 0.")
    }

    /**
     * Set a [Token][K]-[Value][V] pair in cache. If the cache is at [maxSize],
     * remove [trashSize]-number [entries][evictTarget] then add the new entry.
     * @return the [value][V] previously at [key]
     */
    override fun put(key: K, value: V): V? {
        if (key !in this) {
            // Add key to usage ranks
            usageRanks += key
            // Downsize on max-size
            if (size == maxSize) {
                val removeMap = mutableMapOf<K, V?>()
                var i = 0
                while (size > minSize && i++ < trashSize) evictTarget?.also {
                    removeMap[key] = this.remove(it)
                } ?: break
                GlobalScope.launch { trashAction(removeMap) }
            }
        }
        return map.put(key, value)
    }

    /**
     * Retrieve a [value][V] and set to MOST recently used
     *
     * @return the [value][V] at [key] or null
     */
    override fun get(key: K): V? = map[key]?.also {
        usageRanks -= key
        usageRanks.add(key)
    }

    operator fun minusAssign(key: K) = map.minusAssign(
        key).also { usageRanks.remove(key) }

    override fun containsKey(key: K) = map.containsKey(key)

    override fun containsValue(value: V) = map.containsValue(value)

    override fun isEmpty() = map.isEmpty()

    override fun clear() {
        map.clear()
        usageRanks.clear()
    }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from)
        usageRanks += from.keys
    }

    override fun remove(key: K): V? = map.remove(key)

    companion object {
        const val DEFAULT_TRASH_SIZE = 1
    }
}
