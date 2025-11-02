package dev.kura.net.event.impl

import dev.kura.net.event.CancellableEvent

class PostPlayerUpdateEvent : CancellableEvent() {
    private var iterations = 0

    fun getIterations(): Int {
        return iterations
    }

    fun setIterations(i: Int) {
        iterations = i
    }
}