package dev.m7thh4ck.net.mod.module.impl.combat

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.TickEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.Wrapper.playerController
import dev.m7thh4ck.net.util.player.InventoryUtil.inventoryAndHotbarSlots
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.Vec3d
import java.util.concurrent.atomic.AtomicInteger

object AutoTotem : Module(
    name = "AutoTotem",
    category = Category.Combat
) {
    private var strict = setting("Strict", false)
    private var packetListen = false
    private var preferredTotemSlot = 0
    private var numOfTotems = 0

    init {
        eventListener<TickEvent.Post> {
            if (!findTotems() || (mc.currentScreen is ScreenHandlerContext && mc.currentScreen !is InventoryScreen)) {
                packetListen = false
                return@eventListener
            }
            if (player.offHandStack.item != Items.TOTEM_OF_UNDYING) {
                packetListen = true
                val offhandEmptyPreSwitch = player.offHandStack.item == Items.AIR
                legitBypass(preferredTotemSlot)
                legitBypass(45)
                if (!offhandEmptyPreSwitch) {
                    legitBypass(preferredTotemSlot)
                }
            } else {
                packetListen = false
            }
        }
    }

    private fun legitBypass(slot: Int) {
        runCatching {
            if (strict.value) {
                player.velocity = Vec3d.ZERO
            }
            playerController.clickSlot(player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, player)
        }
    }

    private fun findTotems(): Boolean {
        numOfTotems = 0
        val preferredTotemSlotStackSize = AtomicInteger()
        preferredTotemSlotStackSize.set(Int.MIN_VALUE)
        inventoryAndHotbarSlots.forEach { (slotKey: Int, slotValue: ItemStack) ->
            var numOfTotemsInStack = 0
            if (slotValue.item == Items.TOTEM_OF_UNDYING) {
                numOfTotemsInStack = slotValue.count
                if (preferredTotemSlotStackSize.get() < numOfTotemsInStack) {
                    preferredTotemSlotStackSize.set(numOfTotemsInStack)
                    preferredTotemSlot = slotKey
                }
            }
            numOfTotems += numOfTotemsInStack
        }
        if (player.offHandStack.item == Items.TOTEM_OF_UNDYING) {
            numOfTotems += player.offHandStack.count
        }
        return numOfTotems != 0
    }

    override fun getHudInfo(): String {
        return numOfTotems.toString() + ""
    }
}