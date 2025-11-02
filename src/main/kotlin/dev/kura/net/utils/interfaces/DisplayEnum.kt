package dev.kura.net.utils.interfaces

interface DisplayEnum {
    val displayName: CharSequence
    val displayString: String
        get() = displayName.toString()
}