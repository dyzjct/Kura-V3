package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event
import net.minecraft.entity.player.PlayerEntity

class PlayerDiedEvent(val entity: PlayerEntity) : Event()