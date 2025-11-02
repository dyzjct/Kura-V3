package dev.m7thh4ck.net.mod.module.impl.player

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.PacketEvent
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket


object NoFall : Module("NoFall", Category.Player) {

    private val mode by setting("Mode", Mode.Packet)


    init {
        eventListener<PacketEvent.Send> {
            if (fullNullCheck()) return@eventListener
            if (mode != Mode.Packet || player.abilities.creativeMode || it.packet !is PlayerMoveC2SPacket) return@eventListener
            it.packet.onGround = true
        }
    }


    enum class Mode(override val displayName: CharSequence) : DisplayEnum {
        Packet("Packet")
    }

}