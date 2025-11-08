package dev.m7thh4ck.net.util.player

import dev.m7thh4ck.net.util.Util
import net.minecraft.util.Hand

object ItemResult : Util() {
    data class ItemResult(val slot: Int, val count: Int) {
        fun found(): Boolean {
            return slot != -1
        }

        fun holding(): Boolean {
            return if (!found()) false else player.getInventory().selectedSlot === slot
        }

        val hand: Hand?
            get() {
                if (slot == SlotUtils.OFFHAND) return Hand.OFF_HAND else if (slot == player.getInventory().selectedSlot) return Hand.MAIN_HAND
                return null
            }
        val isMainHand: Boolean
            get() = hand == Hand.MAIN_HAND
        val isOffhand: Boolean
            get() = hand == Hand.OFF_HAND
        val isHotbar: Boolean
            get() = slot >= SlotUtils.HOTBAR_START && slot <= SlotUtils.HOTBAR_END
        val isMain: Boolean
            get() = slot >= SlotUtils.MAIN_START && slot <= SlotUtils.MAIN_END
        val isArmor: Boolean
            get() = slot >= SlotUtils.ARMOR_START && slot <= SlotUtils.ARMOR_END
    }
    val slot: Int
        get() {
            TODO()
        }

    fun isOffhand(): Boolean {
        return getHand() == Hand.OFF_HAND
    }
    fun getHand(): Hand? {
        if (slot == SlotUtils.OFFHAND) return Hand.OFF_HAND else if (slot == player.getInventory().selectedSlot) return Hand.MAIN_HAND
        return null
    }
    fun found(): Boolean {
        return slot != -1
    }

    fun holding(): Boolean {
        return if (!found()) false else player.getInventory().selectedSlot === slot
    }

    fun isMainHand(): Boolean {
        return getHand() == Hand.MAIN_HAND
    }

    fun isHotbar(): Boolean {
        return slot >= SlotUtils.HOTBAR_START && slot <= SlotUtils.HOTBAR_END
    }

    fun isMain(): Boolean {
        return slot >= SlotUtils.MAIN_START && slot <= SlotUtils.MAIN_END
    }

    fun isArmor(): Boolean {
        return slot >= SlotUtils.ARMOR_START && slot <= SlotUtils.ARMOR_END
    }
}