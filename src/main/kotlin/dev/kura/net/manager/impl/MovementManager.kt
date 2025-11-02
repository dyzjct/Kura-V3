package dev.kura.net.manager.impl

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.util.Util
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import kotlin.math.hypot
import kotlin.math.max

object MovementManager : Util() {
    var currentPlayerSpeed = 0.0
    var boostSpeed = 0.0

    init {
        eventListener<PacketEvent.Receive>(Int.MAX_VALUE, true) { event ->
            if (event.packet is EntityVelocityUpdateS2CPacket) {
                if (event.packet.id == player.id) {
                    boostSpeed = hypot(
                        event.packet.velocityX / 8000.0,
                        event.packet.velocityZ / 8000.0
                    )
                }
            }
        }

        eventListener<PacketEvent.ReceivePost>(Int.MAX_VALUE, true) { event ->
            if (event.packet is EntityVelocityUpdateS2CPacket) {
                if (event.packet.id == player.id) {
                    boostSpeed = max(
                        boostSpeed, hypot(
                            event.packet.velocityX / 8000.0,
                            event.packet.velocityZ / 8000.0
                        )
                    )
                }
            }
        }

        eventListener<TickEvent.Post>(Int.MAX_VALUE) {
            currentPlayerSpeed = hypot((player.x - player.prevX), (player.z - player.prevZ))
        }
    }

    fun boostReset() {
        boostSpeed = 0.0
    }
}