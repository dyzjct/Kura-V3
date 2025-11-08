package dev.m7thh4ck.net.util.player

import dev.m7thh4ck.net.util.Wrapper.mc
import dev.m7thh4ck.net.util.Wrapper.player
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket

data class SearchInvResult(val slot: Int, val found: Boolean, val stack: ItemStack?) {
    val isHolding: Boolean
        get() = if (player == null) false else player.getInventory().selectedSlot === slot
    val isInHotBar: Boolean
        get() = slot < 9

    fun switchTo() {
        if (found && isInHotBar) switchTo(slot)
    }
    fun switchTo(slot: Int) {
        if (InventoryUtil.player.inventory.selectedSlot == slot) return
        InventoryUtil.player.inventory.selectedSlot = slot
        InventoryUtil.player.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
    }
    companion object {
        private val NOT_FOUND_RESULT = SearchInvResult(-1, false, null)
        fun notFound(): SearchInvResult {
            return NOT_FOUND_RESULT
        }

        fun inOffhand(stack: ItemStack?): SearchInvResult {
            return SearchInvResult(999, true, stack)
        }
    }
}