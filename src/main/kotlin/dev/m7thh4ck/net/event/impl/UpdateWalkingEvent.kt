package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event

sealed class UpdateWalkingEvent {

    class Pre: Event()

    class Post: Event()

}