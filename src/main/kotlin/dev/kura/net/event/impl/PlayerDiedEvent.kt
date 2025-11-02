package dev.kura.net.event.impl

import dev.kura.net.event.Event
import net.minecraft.entity.player.PlayerEntity

class PlayerDiedEvent(val entity: PlayerEntity) : Event()