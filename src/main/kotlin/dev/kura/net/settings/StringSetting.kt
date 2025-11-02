package dev.kura.net.settings

class StringSetting(
    displayName: CharSequence,
    value: String,
    visibility: () -> Boolean = { true }
) : AbstractSetting<String>(displayName, value, visibility)