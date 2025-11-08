package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.CancellableEvent
import net.minecraft.entity.Entity

class RenderLivingLabelEvent(
    val entity: Entity
): CancellableEvent()