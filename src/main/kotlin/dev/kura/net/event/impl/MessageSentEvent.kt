package dev.kura.net.event.impl

import dev.kura.net.event.CancellableEvent


class MessageSentEvent(var message: String) : CancellableEvent()