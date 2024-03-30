package ch.hippmann.godot.utilities.data

import java.lang.ref.WeakReference

class WeakRefMap<KEY, VALUE: Any>: MutableMap<KEY, VALUE> {
    private val internalMap = HashMap<KEY, WeakReference<VALUE>>()

    override val entries: MutableSet<MutableMap.MutableEntry<KEY, VALUE>>
        get() = internalMap
            .entries
            .mapNotNull { (key, value) ->
                value.get()?.let { ref -> key to ref } ?: run {
                    internalMap.remove(key)
                    null
                }
            }
            .toMap()
            .toMutableMap()
            .entries

    override val keys: MutableSet<KEY>
        get() = internalMap.keys

    override val size: Int
        get() = internalMap.size

    override val values: MutableCollection<VALUE>
        get() = internalMap
            .entries
            .mapNotNull { (key, value) -> value.get() ?: run {
                internalMap.remove(key)
                null
            } }
            .toMutableList()

    override fun clear() {
        internalMap.clear()
    }

    override fun isEmpty(): Boolean {
        return internalMap.isEmpty()
    }

    override fun remove(key: KEY): VALUE? {
        return internalMap.remove(key)?.get()
    }

    override fun putAll(from: Map<out KEY, VALUE>) {
        internalMap.putAll(
            from
                .mapValues { (_, value) -> WeakReference(value) }
        )
    }

    override fun put(key: KEY, value: VALUE): VALUE? {
        return internalMap.put(key, WeakReference(value))?.get()
    }

    override fun get(key: KEY): VALUE? {
        return internalMap[key]?.get() ?: run {
            internalMap.remove(key)
            null
        }
    }

    override fun containsValue(value: VALUE): Boolean {
        return internalMap.containsValue(WeakReference(value))
    }

    override fun containsKey(key: KEY): Boolean {
        return internalMap.containsKey(key)
    }
}