package dev.kura.net.settings

import dev.m7thh4ck.net.util.keyboard.Bind

fun <E : Enum<E>> E.next(): E = declaringJavaClass.enumConstants.run {
    get((ordinal + 1) % size)

}

class BooleanSetting @JvmOverloads constructor(
    displayName: CharSequence,
    value: Boolean,
    visibility: () -> Boolean = { true }
): AbstractSetting<Boolean>(displayName, value, visibility) {
    fun isEnabled() {

    }
}


class KeyBindSetting @JvmOverloads constructor(
    displayName: CharSequence,
    value: Bind,
    val func: () -> Unit,
    visibility: () -> Boolean = { true }
): AbstractSetting<Bind>(displayName, value, visibility)

class EnumSetting <E>(
    displayName: CharSequence,
    value: E,
    visibility: () -> Boolean = { true }
): AbstractSetting<E>(displayName, value, visibility) where E : Enum<E> {
    fun nextValue() {
        value = value::class.java.enumConstants[(value.ordinal + 1) % value::class.java.enumConstants.size]
    }

    fun setValueByString(name: String) {
        for (i in value::class.java.enumConstants) {
            if (i.name == name) {
                value = i
            }
        }
    }
}