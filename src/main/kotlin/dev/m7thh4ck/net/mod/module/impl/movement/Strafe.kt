package dev.m7thh4ck.net.mod.module.impl.movement

import dev.kura.net.asmimpl.IVec3d
import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMoveEvent
import dev.m7thh4ck.net.managers.impl.MovementManager
import dev.m7thh4ck.net.managers.impl.MovementManager.boostSpeed
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.entity.EntityUtil.isInBurrow
import dev.m7thh4ck.net.util.entity.EntityUtil.isMoving
import dev.m7thh4ck.net.util.player.PlayerUtil.baseMoveSpeed
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

object Strafe :
    Module(name = "Strafe", category = Category.Movement) {
    private val mode by setting("Mode", Mode.NORMAL)
    private var boost by setting("DamageBoost", false)
    private var eatingCheck by setting("EatingCheck", true)
    private var burrowDetect by setting("BurrowDetect", true)
    private var stage = 1
    private var moveSpeed = 0.0

    init {
        eventListener<PlayerMoveEvent> { event ->
            if (fullNullCheck()) return@eventListener
            if (player.isFallFlying || (player.isSneaking && (!isInBurrow() || !burrowDetect))) return@eventListener
            if (player.isUsingItem && eatingCheck) return@eventListener
            if (Flight.isEnabled()) return@eventListener
            if (shouldReturn() && !player.isTouchingWater && !player.isInLava) {
                if (player.isOnGround) {
                    stage = 2
                }
                when (stage) {
                    0 -> {
                        ++stage
                        MovementManager.currentPlayerSpeed = 0.0
                    }

                    2 -> {
                        if (player.isOnGround && mc.options.jumpKey.isPressed) {
                            if (player.getStatusEffect(StatusEffects.SPEED) != null) {
                                event.movement = Vec3d(event.movement.x, player.velocity.y, event.movement.z)
                                moveSpeed *= if (mode == Mode.NORMAL) 1.7 else 2.149
                            }
                        }
                    }

                    3 -> {
                        moveSpeed =
                            MovementManager.currentPlayerSpeed - (if (mode == Mode.NORMAL) 0.6901 else 0.795) * (MovementManager.currentPlayerSpeed - baseMoveSpeed())
                    }

                    else -> {
                        if (world.getBlockCollisions(
                                player, player.boundingBox.offset(0.0, player.velocity.getY(), 0.0)
                            ).iterator().hasNext() || player.verticalCollision
                        ) {
                            stage =
                                if (player.input.movementForward != 0.0f || player.input.movementSideways != 0.0f) 1 else 0
                        }
                        moveSpeed =
                            MovementManager.currentPlayerSpeed - 0.66 * (MovementManager.currentPlayerSpeed - baseMoveSpeed())
                    }
                }
                if (boost && boostSpeed != 0.0 && isMoving()) {
                    moveSpeed = boostSpeed
                    MovementManager.boostReset()
                }
                moveSpeed = if (!mc.options.jumpKey.isPressed && player.isOnGround) {
                    baseMoveSpeed()
                } else {
                    moveSpeed.coerceAtLeast(baseMoveSpeed())
                }
                if (burrowDetect && isInBurrow()) {
                    moveSpeed = 0.2873 * 0.1f
                }
                if (player.input.movementForward.toDouble() == 0.0 && player.input.movementSideways.toDouble() == 0.0) {
                    event.setSpeed(0.0)
                } else if (player.input.movementForward.toDouble() != 0.0 && player.input.movementSideways.toDouble() != 0.0) {
                    player.input.movementForward *= sin(0.7853981633974483).toFloat()
                    player.input.movementSideways *= cos(0.7853981633974483).toFloat()
                }
                (event.movement as IVec3d).setXZ(
                    (player.input.movementForward * moveSpeed * -sin(
                        Math.toRadians(player.yaw.toDouble())
                    ) + player.input.movementSideways * moveSpeed * cos(
                        Math.toRadians(
                            player.yaw.toDouble()
                        )
                    )) * if (mode == Mode.NORMAL) 0.993 else 0.99,
                    (player.input.movementForward * moveSpeed * cos(
                        Math.toRadians(player.yaw.toDouble())
                    ) - player.input.movementSideways * moveSpeed * -sin(
                        Math.toRadians(
                            player.yaw.toDouble()
                        )
                    )) * if (mode == Mode.NORMAL) 0.993 else 0.99
                )
                event.setSpeed(moveSpeed * if (mode == Mode.NORMAL) 0.993 else 0.99)
                ++stage
            }
        }
    }

    override fun getHudInfo(): String {
        return if (mode == Mode.NORMAL) "Normal" else "Strict"
    }

    private fun shouldReturn(): Boolean {
        return !Speed.isEnabled()
    }

    @Suppress("unused")
    enum class Mode {
        STRICT, NORMAL
    }
}