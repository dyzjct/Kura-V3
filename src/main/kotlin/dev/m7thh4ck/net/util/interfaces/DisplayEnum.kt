package dev.m7thh4ck.net.util.interfaces

interface DisplayEnum {
    val displayName: CharSequence
    val displayString: String
        get() = displayName.toString()
}