package dev.kura.net.event.impl

import dev.kura.net.event.Event

sealed class UpdateWalkingEvent {

    class Pre: Event()

    class Post: Event()

}