@file:Suppress("FunctionName")

package dev.m7thh4ck.net.util.other

import java.util.*

inline fun <reified K : Enum<K>, V> EnumMap(): EnumMap<K, V> {
    return EnumMap<K, V>(K::class.java)
}

inline fun <reified E : Enum<E>> EnumSet(): EnumSet<E> {
    return EnumSet.noneOf(E::class.java)
}
