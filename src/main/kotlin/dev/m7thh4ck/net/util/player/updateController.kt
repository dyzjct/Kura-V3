package dev.m7thh4ck.net.util.player

import dev.m7thh4ck.net.util.Wrapper.connection
import dev.m7thh4ck.net.util.Wrapper.player
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