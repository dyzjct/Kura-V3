package dev.m7thh4ck.net.mod.module.impl.movement

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.GameLoopEvent
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import kotlin.math.cos
import kotlin.math.sin

object Flight : Module("Flight", Category.Movement) {

    private val mode by setting("Mode", Mode.Vanilla)
    private val hSpeed by setting("Horizontal", 1.0f, 0.0f..10.0f, 0.1f) { mode != Mode.MatrixJump }
    private val vSpeed by setting("Vertical", 1.0f, 0.0f..10.0f, 0.1f) { mode != Mode.MatrixJump }
    private val autoToggle by setting("AutoToggle", false) { mode != Mode.MatrixJump }

    private var prevX = 0.0
    private var prevY = 0.0
    private var prevZ = 0.0
    private var onPosLook = false
    private var flyTicks = 0


    init {

        eventListener<TickEvent.Post> {
            if (fullNullCheck()) return@eventListener

            when (mode) {
                Mode.Vanilla -> {
                    if (isMoving()) {
                        val dir = forward(hSpeed.toDouble())
                        player.setVelocity(dir[0], 0.0, dir[1])
                    } else player.setVelocity(0.0, 0.0, 0.0)

                    if (mc.options.jumpKey.isPressed) player.velocity =
                        player.velocity.add(0.0, vSpeed.toDouble(), 0.0)
                    if (mc.options.sneakKey.isPressed) player.velocity =
                        player.velocity.add(0.0, -vSpeed.toDouble(), 0.0)
                }

                Mode.AirJump -> {
                    if (isMoving() && world.getBlockCollisions(
                            player,
                            player.boundingBox.expand(0.5, 0.0, 0.5)
                                .offset(0.0, -1.0, 0.0)
                        ).iterator().hasNext()
                    ) {
                        player.isOnGround = true
                        player.jump()
                    }
                }

                Mode.MatrixGlide -> {
                    if (player.isOnGround) {
                        player.jump()
                        flyTicks = 5
                    } else if (flyTicks > 0) {
                        if (isMoving()) {
                            val dir: DoubleArray = forward(hSpeed.toDouble())
                            player.setVelocity(dir[0], -0.04, dir[1])
                        } else player.setVelocity(0.0, -0.04, 0.0)
                        flyTicks--
                    }
                }

                else -> {}
            }
        }

        eventListener<GameLoopEvent.Tick> {
            if (fullNullCheck()) return@eventListener

            if (mode != Mode.MatrixJump) return@eventListener
            player.abilities.flying = false
            player.setVelocity(0.0, 0.0, 0.0)

            if (mc.options.jumpKey.isPressed) player.velocity = player.velocity.add(0.0, vSpeed.toDouble(), 0.0)
            if (mc.options.sneakKey.isPressed) player.velocity = player.velocity.add(0.0, -vSpeed.toDouble(), 0.0)

            val dir: DoubleArray = forward(hSpeed.toDouble())
            player.setVelocity(dir[0], player.velocity.getY(), dir[1])
        }

        eventListener<PacketEvent.Receive> {

            if (fullNullCheck()) return@eventListener

            if (mode == Mode.MatrixJump) {
                if (it.packet is PlayerPositionLookS2CPacket) {
                    onPosLook = true
                    prevX = player.velocity.getX()
                    prevY = player.velocity.getY()
                    prevZ = player.velocity.getZ()
                }
            }
        }

        eventListener<PacketEvent.Send> {

            if (fullNullCheck()) return@eventListener

            if (mode == Mode.MatrixJump) {
                if (it.packet is PlayerMoveC2SPacket.Full) {
                    if (onPosLook) {
                        player.setVelocity(prevX, prevY, prevZ)
                        onPosLook = false
                        if (autoToggle) disable()
                    }
                }
            }

        }


    }

    private fun isMoving(): Boolean = player.input.movementForward != 0.0f || player.input.movementSideways != 0.0f

    private fun forward(d: Double): DoubleArray {
        var f = player.input.movementForward
        var f2 = player.input.movementSideways
        var f3 = player.yaw
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += (if (f > 0.0f) -45 else 45).toFloat()
            } else if (f2 < 0.0f) {
                f3 += (if (f > 0.0f) 45 else -45).toFloat()
            }
            f2 = 0.0f
            if (f > 0.0f) {
                f = 1.0f
            } else if (f < 0.0f) {
                f = -1.0f
            }
        }
        val d2 = sin(Math.toRadians((f3 + 90.0f).toDouble()))
        val d3 = cos(Math.toRadians((f3 + 90.0f).toDouble()))
        val d4 = f * d * d3 + f2 * d * d2
        val d5 = f * d * d2 - f2 * d * d3
        return doubleArrayOf(d4, d5)
    }

    private enum class Mode(override val displayName: CharSequence) : DisplayEnum {
        Vanilla("Vanilla"),
        MatrixJump("MatrixJump"),
        AirJump("AirJump"),
        MatrixGlide("MatrixGlide")
    }
}