package dev.m7thh4ck.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMotionEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.util.Wrapper.playerController
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.SlotActionType

object AutoReplenish : Module(
    name = "AutoReplenish",
    category = Category.Player
) {
    private val refillWhile by setting("RefillAt", 32, 1..64, 1)
    private val tickDelay by setting("TickDelay", 1, 0..10, 1)
    private val checkName by setting("CheckName", false)
    private val checkDamage by setting("CheckDamage", false)
    private var delayStep = 0

    init {
        eventListener<PlayerMotionEvent> {
            if (mc.currentScreen is ScreenHandlerContext && mc.currentScreen !is InventoryScreen) {
                return@eventListener
            }
            delayStep = if (delayStep < tickDelay) {
                delayStep++
                return@eventListener
            } else {
                0
            }
            val slots = findReplenishableHotbarSlot() ?: return@eventListener
            val inventorySlot = slots.key

            playerController.clickSlot(
                player.currentScreenHandler.syncId,
                inventorySlot,
                0,
                SlotActionType.QUICK_MOVE,
                player
            )
        }
    }

    private fun findReplenishableHotbarSlot(): Pair<Int, Int>? {
        var returnPair: Pair<Int, Int>? = null
        for ((key, stack) in hotbar) {
            if (stack.isEmpty || stack.item == Items.AIR) {
                continue
            }
            if (!stack.isStackable) {
                continue
            }
            if (stack.count >= stack.maxCount) {
                continue
            }
            if (stack.count > refillWhile) {
                continue
            }
            val inventorySlot = findCompatibleInventorySlot(stack)
            if (inventorySlot == -1) {
                continue
            }
            returnPair = Pair(inventorySlot, key)
        }
        return returnPair
    }

    private fun findCompatibleInventorySlot(hotbarStack: ItemStack): Int {
        var inventorySlot = -1
        var smallestStackSize = 999
        for ((key, inventoryStack) in inventory) {
            if (inventoryStack.isEmpty || inventoryStack.item === Items.AIR) {
                continue
            }
            if (!isCompatibleStacks(hotbarStack, inventoryStack)) {
                continue
            }
            val currentStackSize = player.inventory.getStack(key).count
            if (smallestStackSize > currentStackSize) {
                smallestStackSize = currentStackSize
                inventorySlot = key
            }
        }
        return inventorySlot
    }

    private fun isCompatibleStacks(stack1: ItemStack, stack2: ItemStack): Boolean {
        if (stack1.item != stack2.item) {
            return false
        }

        if (stack1.item is BlockItem && stack2.item is BlockItem) {
            val block1 = (stack1.item as BlockItem).block
            val block2 = (stack2.item as BlockItem).block
            if (block1.defaultState != block2.defaultState) {
                return false
            }
        }

        if (checkName && stack1.name != stack2.name) {
            return false
        }

        if (checkDamage && stack1.damage == stack2.damage) {
            return false
        }

        return true
    }


    private val inventory: Map<Int, ItemStack>
        get() = getInventorySlots(9, 35)
    private val hotbar: Map<Int, ItemStack>
        get() = getInventorySlots(36, 44)

    private fun getInventorySlots(current: Int, last: Int): Map<Int, ItemStack> {
        val fullInventorySlots: MutableMap<Int, ItemStack> = HashMap()
        for (i in current..last) {
            fullInventorySlots[i] = player.playerScreenHandler.stacks[i]
        }
        return fullInventorySlots
    }

    class Pair<T, S>(var key: T, var value: S)
}