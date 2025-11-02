package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event

class MouseClickEvent(
    actionCode: Int,
    val buttonCode: Int
) : Event() {
    val action = if (actionCode == 0) MouseAction.RELEASE else MouseAction.PRESS
    val button = when (buttonCode) {
        0 -> MouseButton.LEFT
        1 -> MouseButton.RIGHT
        2 -> MouseButton.MIDDLE
        else -> MouseButton.UNKNOWN
    }

    enum class MouseAction {
        PRESS,
        RELEASE
    }

    enum class MouseButton {
        RIGHT,
        LEFT,
        MIDDLE,
        UNKNOWN
    }
}