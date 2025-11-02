package dev.m7thh4ck.net.mod.module.impl.misc

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.MessageSentEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module

object ChatSuffix : Module("ChatSuffix", Category.Misc) {

    private var skip: String = ""

    init {
        eventListener<MessageSentEvent> {
            it.message += " \uD835\uDD78₇\uD835\uDD99ℌん₄\uD835\uDE8Ck"
        }
    }
}
