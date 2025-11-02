package dev.kura.net.utils.player

import dev.kura.net.utils.Util
import dev.kura.net.utils.Wrapper
import dev.kura.net.utils.Wrapper.connection
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.minecraft.block.AirBlock
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.BlockPos
import java.util.*


object InventoryUtil : Util() {

    fun findItemInHotbar(item: Item): Int? {
        for (i in 0 until 9) {
            if (player.inventory.getStack(i).item == item) return i
        }
        return null
    }



    fun switchTo(slot: Int) {
        if (player.inventory.selectedSlot == slot) return
        player.inventory.selectedSlot = slot
        player.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
    }

    fun moveTo(from: Int, to: Int) {
        val handler = player.currentScreenHandler
        val stack = Int2ObjectArrayMap<ItemStack>()
        stack.put(to, handler.getSlot(to).stack)
        connection.sendPacket(
            ClickSlotC2SPacket(
                handler.syncId,
                handler.revision,
                PlayerInventory.MAIN_SIZE + from,
                to,
                SlotActionType.SWAP,
                handler.cursorStack.copy(),
                stack
            )
        )
    }

    fun findItemInInv(item: Item): Int? {
        for (i in 0..player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (stack.item == item) return i
        }
        return null
    }

    fun getItemCount(item: Item): Int {
        var count = 0
        for (i in 0..player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (stack.item == item) ++count
        }
        return count
    }

    fun spoofHotbar(slot: Int, func: () -> Unit) {
        val oldSlot = player.inventory.selectedSlot
        switchTo(slot)
        func.invoke()
        switchTo(oldSlot)
    }

    fun spoofHotbarBypass(slot: Int, func: () -> Unit) {
        move(player.inventory.selectedSlot, slot)
        func.invoke()
        move(player.inventory.selectedSlot, slot)
    }

    fun move(from: Int, to: Int) {
        val handler = player.currentScreenHandler
        val stack = Int2ObjectArrayMap<ItemStack>()
        stack.put(to, handler.getSlot(to).stack)
        connection.sendPacket(
            ClickSlotC2SPacket(
                handler.syncId,
                handler.revision,
                PlayerInventory.MAIN_SIZE + from,
                to,
                SlotActionType.SWAP,
                handler.cursorStack.copy(),
                stack
            )
        )
    }

    private fun doMove(from: Int, to: Int) {
        val handler = player.currentScreenHandler
        val stack = Int2ObjectArrayMap<ItemStack>()
        stack.put(to, handler.getSlot(to).stack)
        connection.sendPacket(
            ClickSlotC2SPacket(
                handler.syncId,
                handler.revision,
                PlayerInventory.MAIN_SIZE + from,
                to,
                SlotActionType.SWAP,
                handler.cursorStack.copy(),
                stack
            )
        )
    }


    //   fun spoofHotbarBypass(slot: Int, func: () -> Unit) {
    //      val handSlot = player.inventory.selectedSlot
    //      moveTo(handSlot, slot)
    //       func.invoke()
    //       moveTo(handSlot, handSlot)
    //       playerController.updateController()
    //   }

    fun findBestSlot(pos: BlockPos): Int {
        var index = -1
        var currentFastest = 1f
        if (Wrapper.fullNullCheck()) return -1
        if (world.getBlockState(pos).block is AirBlock) return -1
        for (i in 9..44) {
            val stack = player.inventory.getStack(if (i >= 36) i - 36 else i)
            if (stack != ItemStack.EMPTY) {
                if (stack.maxDamage - stack.damage <= 10) continue
                val digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack).toFloat()
                val destroySpeed = stack.getMiningSpeedMultiplier(world.getBlockState(pos))
                if (digSpeed + destroySpeed > currentFastest) {
                    currentFastest = digSpeed + destroySpeed
                    index = i
                }
            }
        }
        return if (index >= 36) index - 36 else index
    }

    val inventoryAndHotbarSlots: Map<Int, ItemStack>
        get() = getInventorySlots(9)

    fun getInventorySlots(current: Int): Map<Int, ItemStack> {
        var currentSlot = current
        val fullInventorySlots: MutableMap<Int, ItemStack> = HashMap()
        while (currentSlot <= 44) {
            fullInventorySlots[currentSlot] = player.inventory.getStack(currentSlot)
            currentSlot++
        }
        return fullInventorySlots
    }

    interface Searcher {
        fun isValid(stack: ItemStack?): Boolean
    }
}