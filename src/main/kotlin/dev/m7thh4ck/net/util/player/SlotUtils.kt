package dev.m7thh4ck.net.util.player

import dev.m7thh4ck.net.util.Util
import net.minecraft.screen.*

object SlotUtils : Util() {
    const val HOTBAR_START = 0
    const val HOTBAR_END = 8

    const val OFFHAND = 45

    const val MAIN_START = 9
    const val MAIN_END = 35

    const val ARMOR_START = 36
    const val ARMOR_END = 39

    fun indexToId(i: Int): Int {
        if (mc.player == null) return -1
        val handler = mc.player!!.currentScreenHandler
        if (handler is PlayerScreenHandler) return survivalInventory(i) else if (handler is GenericContainerScreenHandler) return genericContainer(
            i,
            handler.rows
        ) else if (handler is CraftingScreenHandler) return craftingTable(i) else if (handler is FurnaceScreenHandler) return furnace(
            i
        ) else if (handler is BlastFurnaceScreenHandler) return furnace(i) else if (handler is SmokerScreenHandler) return furnace(
            i
        ) else if (handler is Generic3x3ContainerScreenHandler) return generic3x3(i) else if (handler is EnchantmentScreenHandler) return enchantmentTable(
            i
        ) else if (handler is BrewingStandScreenHandler) return brewingStand(i) else if (handler is MerchantScreenHandler) return villager(
            i
        ) else if (handler is BeaconScreenHandler) return beacon(i) else if (handler is AnvilScreenHandler) return anvil(
            i
        ) else if (handler is HopperScreenHandler) return hopper(i) else if (handler is ShulkerBoxScreenHandler) return genericContainer(
            i,
            3
        ) else if (handler is CartographyTableScreenHandler) return cartographyTable(i) else if (handler is GrindstoneScreenHandler) return grindstone(
            i
        ) else if (handler is LecternScreenHandler) return lectern() else if (handler is LoomScreenHandler) return loom(
            i
        ) else if (handler is StonecutterScreenHandler) return stonecutter(i)
        return -1
    }

    private fun survivalInventory(i: Int): Int {
        if (isHotbar(i)) return 36 + i
        return if (isArmor(i)) 5 + (i - 36) else i
    }

    private fun genericContainer(i: Int, rows: Int): Int {
        if (isHotbar(i)) return (rows + 3) * 9 + i
        return if (isMain(i)) rows * 9 + (i - 9) else -1
    }

    private fun craftingTable(i: Int): Int {
        if (isHotbar(i)) return 37 + i
        return if (isMain(i)) i + 1 else -1
    }

    private fun furnace(i: Int): Int {
        if (isHotbar(i)) return 30 + i
        return if (isMain(i)) 3 + (i - 9) else -1
    }

    private fun generic3x3(i: Int): Int {
        if (isHotbar(i)) return 36 + i
        return if (isMain(i)) i else -1
    }

    private fun enchantmentTable(i: Int): Int {
        if (isHotbar(i)) return 29 + i
        return if (isMain(i)) 2 + (i - 9) else -1
    }

    private fun brewingStand(i: Int): Int {
        if (isHotbar(i)) return 32 + i
        return if (isMain(i)) 5 + (i - 9) else -1
    }

    private fun villager(i: Int): Int {
        if (isHotbar(i)) return 30 + i
        return if (isMain(i)) 3 + (i - 9) else -1
    }

    private fun beacon(i: Int): Int {
        if (isHotbar(i)) return 28 + i
        return if (isMain(i)) 1 + (i - 9) else -1
    }

    private fun anvil(i: Int): Int {
        if (isHotbar(i)) return 30 + i
        return if (isMain(i)) 3 + (i - 9) else -1
    }

    private fun hopper(i: Int): Int {
        if (isHotbar(i)) return 32 + i
        return if (isMain(i)) 5 + (i - 9) else -1
    }

    private fun cartographyTable(i: Int): Int {
        if (isHotbar(i)) return 30 + i
        return if (isMain(i)) 3 + (i - 9) else -1
    }

    private fun grindstone(i: Int): Int {
        if (isHotbar(i)) return 30 + i
        return if (isMain(i)) 3 + (i - 9) else -1
    }

    private fun lectern(): Int {
        return -1
    }

    private fun loom(i: Int): Int {
        if (isHotbar(i)) return 31 + i
        return if (isMain(i)) 4 + (i - 9) else -1
    }

    private fun stonecutter(i: Int): Int {
        if (isHotbar(i)) return 29 + i
        return if (isMain(i)) 2 + (i - 9) else -1
    }

    // Utils

    // Utils
    fun isHotbar(i: Int): Boolean {
        return i >= HOTBAR_START && i <= HOTBAR_END
    }

    fun isMain(i: Int): Boolean {
        return i >= MAIN_START && i <= MAIN_END
    }

    fun isArmor(i: Int): Boolean {
        return i >= ARMOR_START && i <= ARMOR_END
    }
}