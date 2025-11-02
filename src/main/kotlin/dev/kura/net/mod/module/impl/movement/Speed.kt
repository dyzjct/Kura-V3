package dev.kura.net.mod.module.impl.movement

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMoveEvent
import dev.kura.net.manager.impl.MovementManager
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.kura.net.utils.entity.EntityUtil.isInBurrow
import dev.kura.net.utils.entity.EntityUtil.isMoving
import dev.kura.net.utils.entity.EntityUtil.jumpSpeed
import dev.kura.net.utils.player.PlayerUtil.baseMoveSpeed
import net.minecraft.entity.effect.StatusEffects

object Speed : Module(name = "Speed", category = Category.Movement) {
    private var damageBoost by setting("DamageBoost", true)
    private var strict by setting("Strict", true)
    private var burrowDetect by setting("BurrowDetect", true)
    private var baseSpeed = 0.0
    private var stage = 0
    private var ticks = 0


    init {
        onEnable {
            stage = 1
            ticks = 0
            baseSpeed = 0.2873
        }
        eventListener<PlayerMoveEvent> { event ->
            if (player.abilities.flying) return@eventListener
            if (player.isFallFlying) return@eventListener
            if (player.hungerManager.foodLevel <= 6) return@eventListener
            if (event.isCancelled) return@eventListener
            event.cancel()
            if (isMoving()) {
                if (stage == 1 && player.isOnGround) {
                    player.setVelocity(
                        player.velocity.x,
                        jumpSpeed,
                        player.velocity.z
                    )
                    event.movement.y = jumpSpeed
                    baseSpeed *= 2.149
                    stage = 2
                } else if (stage == 2) {
                    baseSpeed =
                        MovementManager.currentPlayerSpeed - 0.66 * (MovementManager.currentPlayerSpeed - baseMoveSpeed())
                    stage = 3
                } else {
                    if (world.getBlockCollisions(
                            player,
                            player.boundingBox.offset(0.0, player.velocity.getY(), 0.0)
                        ).iterator().hasNext() || player.verticalCollision
                    ) stage = 1
                    baseSpeed =
                        MovementManager.currentPlayerSpeed - MovementManager.currentPlayerSpeed / 159.0
                }
                baseSpeed = baseSpeed.coerceAtLeast(baseMoveSpeed())
                var baseStrictSpeed =
                    if (strict || player.input.movementForward < 1) 0.465 else 0.576
                var baseRestrictedSpeed =
                    if (strict || player.input.movementForward < 1) 0.44 else 0.57
                if (player.hasStatusEffect(StatusEffects.SPEED)) {
                    val amplifier = player.getStatusEffect(StatusEffects.SPEED)!!.amplifier.toDouble()
                    baseStrictSpeed *= 1 + 0.2 * (amplifier + 1)
                    baseRestrictedSpeed *= 1 + 0.2 * (amplifier + 1)
                }
                if (player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                    val amplifier = player.getStatusEffect(StatusEffects.SLOWNESS)!!.amplifier.toDouble()
                    baseStrictSpeed /= 1 + 0.2 * (amplifier + 1)
                    baseRestrictedSpeed /= 1 + 0.2 * (amplifier + 1)
                }
                baseSpeed = baseSpeed.coerceAtMost(if (ticks > 25) baseStrictSpeed else baseRestrictedSpeed)
                if (damageBoost && MovementManager.boostSpeed != 0.0) {
                    baseSpeed += MovementManager.boostSpeed
                    MovementManager.boostReset()
                }
                if (ticks++ > 50) ticks = 0
                if (burrowDetect && isInBurrow()) {
                    baseSpeed = 0.2873 * 0.1f
                }
                event.setSpeed(baseSpeed)
            } else {
                event.setSpeed(0.0)
            }
        }
    }
}