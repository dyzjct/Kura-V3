package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.StopUsingItemEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround


object Quiver : Module("Quiver", Category.Combat) {
    init {
        eventListener<StopUsingItemEvent> {
            val player = mc.player ?: return@eventListener

            if (player.mainHandStack.item == Items.BOW) {
                RotationManager.stopRotation()
                player.networkHandler.sendPacket(LookAndOnGround(mc.player!!.getYaw(), -90f, mc.player!!.isOnGround))
                RotationManager.startRotation()
                    shoot()
                    return@eventListener
                mc.options.useKey.isPressed = true
            }
        }
    }
    private fun shoot() {
        if (player.mainHandStack.item == Items.BOW) {
            mc.options.useKey.isPressed = false
        }
    }
}