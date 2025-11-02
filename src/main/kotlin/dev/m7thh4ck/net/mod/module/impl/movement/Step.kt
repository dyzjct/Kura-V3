package dev.m7thh4ck.net.mod.module.impl.movement

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMoveEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module

object Step :
    Module(name = "Step", category = Category.Movement) {
    private val StepHeight by setting("StepHeight", 2.0f, 1.0f..5.0f, 0.5f)

    init {
        eventListener<PlayerMoveEvent> {
            if (this.isEnabled()) player.stepHeight = StepHeight
        }
        onDisable { player.stepHeight = 0.6f }
    }
}