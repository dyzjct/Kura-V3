package dev.m7thh4ck.net.mod.module.impl.misc

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.MouseClickEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.Wrapper.playerController
import dev.m7thh4ck.net.util.helper.ChatUtil
import dev.m7thh4ck.net.util.other.ScreenUtils.safeReturn
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbarBypass
import net.minecraft.item.Items
import net.minecraft.util.Hand

object MCP : Module(
    name = "MCP",
    category = Category.Misc
) {
    init {
        eventListener<MouseClickEvent> {
            if (it.button == MouseClickEvent.MouseButton.MIDDLE && it.action == MouseClickEvent.MouseAction.PRESS) {
                if (mc.currentScreen.safeReturn()) return@eventListener
                if (mc.targetedEntity == null) {
                    val slot = InventoryUtil.findItemInInv(Items.ENDER_PEARL) ?: return@eventListener
                    spoofHotbarBypass(slot) {
                        RotationManager.stopRotation()
                        playerController.interactItem(player, Hand.MAIN_HAND)
                        RotationManager.startRotation()
                    }
                } else {
                    ChatUtil.sendRawMessage("${ChatUtil.RED}[MCP WARNING] ${ChatUtil.AQUA}Pearls will be thrown on mobs!!! Cancel throwing!!!")
                }
            }
        }
    }
}