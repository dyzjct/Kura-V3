package dev.m7thh4ck.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket

object NoRotate : Module("NoRotate", Category.Player) {

    init {
        eventListener<PacketEvent.Receive> {
            if (fullNullCheck()) return@eventListener

            if (it.packet is PlayerPositionLookS2CPacket) {
                it.packet.pitch = player.pitch
                it.packet.yaw = player.yaw
            }
        }
    }

}