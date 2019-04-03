package com.sim.ouch.datastructures

import java.util.concurrent.ConcurrentHashMap

/**
 * A Bi-direction [MutableMap].
 * Backed by two [ConcurrentHashMap]
 */
class MutableBiMap<K, V>() : MutableMap<K, V> {

    constructor(vararg entries: Pair<K, V>)
            : this() { entries.forEach { (k, v) -> put(k, v) } }

    private val directMap = ConcurrentHashMap<K, V>()
    private val inverseMap = ConcurrentHashMap<V, K>()

    override val size get() = directMap.size
    override val entries get() = directMap.entries
    override val keys: MutableSet<K> get() = directMap.keys
    override val values: MutableCollection<V> get() = inverseMap.keys

    override operator fun get(key: K) = directMap[key]
    /** Returns the [key][K] associated with the [value]. */
    fun fromValue(value: V) = inverseMap[value]

    override fun put(key: K, value: V): V? {
        inverseMap[value] = key
        return directMap.put(key, value)
    }

    fun putValue(value: V, key: K): K? {
        directMap[key] = value
        return inverseMap.put(value, key)
    }

    override fun putAll(from: Map<out K, V>) {
        directMap.putAll(from)
        from.forEach { (k: K, v: V) -> inverseMap[v] = k }
    }

    override fun remove(key: K) = directMap.remove(key)?.also { inverseMap.remove(it) }

    fun removeValue(value: V) = inverseMap.remove(value)?.also { directMap.remove(it) }

    override fun containsKey(key: K) = directMap.containsKey(key)

    override fun containsValue(value: V) = inverseMap.containsKey(value)

    override fun isEmpty() = directMap.isEmpty() && inverseMap.isEmpty()

    override fun clear() {
        directMap.clear()
        inverseMap.clear()
    }

}
