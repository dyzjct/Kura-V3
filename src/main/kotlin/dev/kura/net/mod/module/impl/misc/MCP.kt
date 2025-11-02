package dev.kura.net.mod.module.impl.misc

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.MouseClickEvent
import dev.kura.net.manager.impl.RotationManager
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.kura.net.utils.Wrapper.playerController
import dev.kura.net.utils.helper.ChatUtil
import dev.kura.net.utils.other.ScreenUtils.safeReturn
import dev.kura.net.utils.player.InventoryUtil
import dev.kura.net.utils.player.InventoryUtil.spoofHotbarBypass
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