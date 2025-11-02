package dev.kura.net.event.impl

import dev.kura.net.event.Event
import net.minecraft.entity.decoration.EndCrystalEntity

class CrystalSetDeadEvent(
    val x: Double,
    val y: Double,
    val z: Double,
    val crystals: List<EndCrystalEntity>
) : Event()