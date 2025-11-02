package dev.kura.net.event.impl

import dev.kura.net.event.Event

sealed class TickEvent: Event() {
    data object Pre : TickEvent()
    data object Post : TickEvent()
}