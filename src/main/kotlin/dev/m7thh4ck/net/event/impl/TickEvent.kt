package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event

sealed class TickEvent: Event() {
    data object Pre : TickEvent()
    data object Post : TickEvent()
}