package dev.m7thh4ck.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.StopUsingItemEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.item.Items

object PacketEat : Module("PacketEat", Category.Player) {

    init {
        eventListener<StopUsingItemEvent> {
            val player = mc.player ?: return@eventListener
            if (player.activeItem.item == Items.ENCHANTED_GOLDEN_APPLE) {
                player.stopUsingItem()
                it.cancel()
            }
        }
    }

}