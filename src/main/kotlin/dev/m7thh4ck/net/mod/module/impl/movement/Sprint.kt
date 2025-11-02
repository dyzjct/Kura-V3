package dev.m7thh4ck.net.mod.module.impl.movement

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerUpdateEvent
import dev.m7thh4ck.net.util.player.PlayerUtil
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module

object Sprint : Module("Sprint", Category.Movement) {

    private val stopWhileUsing by setting("StopWhileUsing", false)

    init {
        eventListener<PlayerUpdateEvent> {

            if (fullNullCheck()) return@eventListener

            if (player.hungerManager.foodLevel <= 6) return@eventListener

            if (player.horizontalCollision) return@eventListener

            if (player.input.movementForward < 0) return@eventListener

            if (player.isSneaking) return@eventListener

            if (player.isUsingItem && stopWhileUsing) return@eventListener

            if (!PlayerUtil.isMoving()) return@eventListener

            player.isSprinting = true
        }
    }

}