package dev.m7thh4ck.net.mod.module.impl.misc

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.MessageSentEvent
import dev.m7thh4ck.net.event.impl.PacketEvent
import dev.m7thh4ck.net.managers.impl.CommandManager
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket

object ChatSuffix : Module("ChatSuffix", Category.Misc) {

    private var skip: String = ""

    init {
        eventListener<MessageSentEvent> {
            it.message += " \uD835\uDD78₇\uD835\uDD99ℌん₄\uD835\uDE8Ck"
        }
    }
}
