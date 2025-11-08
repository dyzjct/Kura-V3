package dev.m7thh4ck.net.mod.module.impl.movement

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.PacketEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket

object Velocity : Module("Velocity", Category.Movement) {

    val blockPush by setting("Block", true)
    val entityPush by setting("Entity", true)
    val waterPush by setting("Water", true)

    init {

        eventListener<PacketEvent.Receive> { event ->
            if (fullNullCheck()) return@eventListener

            if (event.packet is EntityStatusS2CPacket && event.packet.status
                    .toInt() == 31 && event.packet.getEntity(world) is FishingBobberEntity
            ) {
                if ((event.packet.getEntity(world) as FishingBobberEntity).hookedEntity == player) {
                    event.cancel()
                }
            }

            if (event.packet is ExplosionS2CPacket) {
                val packet = event.packet
                packet.playerVelocityX = 0f
                packet.playerVelocityY = 0f
                packet.playerVelocityZ = 0f
                return@eventListener
            }

            if (event.packet is EntityVelocityUpdateS2CPacket) {
                if (event.packet.id == player.id) {
                    event.cancel()
                }
            }
        }

    }

}