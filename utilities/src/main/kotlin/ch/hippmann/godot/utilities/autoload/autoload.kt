package ch.hippmann.godot.utilities.autoload

import ch.hippmann.godot.utilities.data.WeakRefMap
import godot.Node
import godot.core.asNodePath


@PublishedApi
internal val autoloadCache = WeakRefMap<String, Node>()

@Suppress("unused")
inline fun <reified T: Node> Node.autoload(nodeName: String = T::class.java.simpleName): T {
    val node = autoloadCache[nodeName] ?: run {
        val node = getNode("/root/$nodeName".asNodePath())
        if (node != null) {
            autoloadCache[nodeName] = node
        }
        node
    }
    return requireNotNull(node as? T) {
        "Could not find autoload singleton with name $nodeName and type ${T::class.java}"
    }
}

@Suppress("unused")
inline fun <reified T: Node> Node.autoloadSafe(nodeName: String = T::class.java.simpleName): T? {
    val node = autoloadCache[nodeName] ?: run {
        val node = getNode("/root/$nodeName".asNodePath())
        if (node != null) {
            autoloadCache[nodeName] = node
        }
        node
    }
    return node as? T
}