package dev.m7thh4ck.net.settings

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


abstract class AbstractSetting<T>(
    val displayName: CharSequence,
    var value : T,
    val visibility: () -> Boolean
): ReadWriteProperty<Any,T> {
    val defaultValue = value

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }

    fun reset() {
        value = defaultValue
    }

    val isVisible
        get() = visibility
}