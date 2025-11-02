package dev.kura.net.utils.player

import dev.kura.net.utils.Wrapper.connection
import dev.kura.net.utils.Wrapper.player
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket

fun ClientPlayerInteractionManager.updateController() {
    runCatching {
        val i = player.inventory.selectedSlot
        if (i != lastSelectedSlot) {
            lastSelectedSlot = i
            connection.sendPacket(UpdateSelectedSlotC2SPacket(lastSelectedSlot))
        }
    }
}