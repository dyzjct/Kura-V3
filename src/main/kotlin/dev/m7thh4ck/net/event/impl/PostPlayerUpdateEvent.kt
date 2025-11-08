package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.CancellableEvent

class PostPlayerUpdateEvent : CancellableEvent() {
    private var iterations = 0

    fun getIterations(): Int {
        return iterations
    }

    fun setIterations(i: Int) {
        iterations = i
    }
}