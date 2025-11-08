package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.CancellableEvent


class MessageSentEvent(var message: String) : CancellableEvent()