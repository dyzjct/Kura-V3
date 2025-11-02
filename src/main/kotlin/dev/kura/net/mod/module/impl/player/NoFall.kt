package dev.kura.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.utils.interfaces.DisplayEnum
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
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