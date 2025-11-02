package dev.kura.net.manager.impl

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.CrystalSetDeadEvent
import dev.kura.net.event.impl.PacketEvent
import dev.m7thh4ck.net.util.Wrapper
import dev.m7thh4ck.net.util.math.vector.distanceSqTo
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

object CombatManager {
    private var crystalList = emptyList<EndCrystalEntity>()

    fun init() {
        eventListener<PacketEvent.Receive>(10, true) { event ->
            if (fullNullCheck()) return@eventListener
            if (event.packet is PlaySoundS2CPacket) {
                if (event.packet.category != SoundCategory.BLOCKS) return@eventListener
                if (event.packet.sound != SoundEvents.ENTITY_GENERIC_EXPLODE) return@eventListener
                val list = crystalList.asSequence()
                    .filter { it.distanceSqTo(event.packet.x, event.packet.y, event.packet.z) <= 144.0 }
                    .onEach(EndCrystalEntity::kill)
                    .also { e -> e.forEach { it.setRemoved(Entity.RemovalReason.KILLED) } }
                    .toList()

                if (list.isNotEmpty()) {
                    list.forEach {
                        Wrapper.world.removeEntity(it.id, Entity.RemovalReason.DISCARDED)
                    }
                }
                CrystalSetDeadEvent(event.packet.x, event.packet.y, event.packet.z, list).post()
            }
        }
    }


    private val mc = MinecraftClient.getInstance()!!
    private fun fullNullCheck(): Boolean = mc.player == null || mc.world == null

}