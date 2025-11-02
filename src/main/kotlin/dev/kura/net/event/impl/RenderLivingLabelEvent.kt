package dev.kura.net.event.impl

import dev.kura.net.event.CancellableEvent
import net.minecraft.entity.Entity

class RenderLivingLabelEvent(
    val entity: Entity
): CancellableEvent()