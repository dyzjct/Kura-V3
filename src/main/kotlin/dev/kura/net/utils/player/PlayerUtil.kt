package dev.kura.net.utils.player

import dev.kura.net.mod.module.impl.client.Settings
import dev.kura.net.utils.Util
import dev.kura.net.utils.Wrapper.connection
import dev.kura.net.utils.Wrapper.playerController
import dev.kura.net.utils.entity.EntityPredictInfo
import dev.kura.net.utils.entity.EntityUtil.canMove
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.PendingUpdateManager
import net.minecraft.client.network.SequencedPacketCreator
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.*


object PlayerUtil : Util() {

    inline val Entity.realSpeed get() = hypot(x - prevX, z - prevZ)

    fun swingHand() {
        when (Settings.swingHand) {
            Settings.SwingMode.MainHand -> player.swingHand(Hand.MAIN_HAND)
            Settings.SwingMode.OFFHand -> player.swingHand(Hand.OFF_HAND)
            Settings.SwingMode.Packet -> connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
            else -> return
        }
    }

    fun baseMoveSpeed(): Double {
        var n = 0.2873
        if (player.hasStatusEffect(StatusEffects.SPEED)) {
            player.getStatusEffect(StatusEffects.SPEED)?.let {
                n *= 1.0 + 0.2 * (it.amplifier + 1)
            }
        }
        return n
    }

    fun getWorldActionId(world: ClientWorld): Int {
        val pum: PendingUpdateManager = getUpdateManager(world)
        val p = pum.sequence
        pum.close()
        return p
    }

    fun getHealth(): Float {
        val livingBase = player as LivingEntity
        return livingBase.health + livingBase.absorptionAmount
    }

    private fun getUpdateManager(world: ClientWorld): PendingUpdateManager {
        return world.pendingUpdateManager
    }

    fun LivingEntity.getPredictInfo(ticks: Int): EntityPredictInfo {
        val motionX = this.velocity.x.coerceIn(-0.6, 0.6)
        val motionY = this.velocity.y.coerceIn(-0.6, 0.6)
        val motionZ = this.velocity.z.coerceIn(-0.6, 0.6)

        val entityBox = this.boundingBox
        var targetBox = entityBox

        for (tick in 0..ticks) {
            targetBox =
                canMove(targetBox, motionX, motionY, motionZ)
                    ?: canMove(targetBox, motionX, 0.0, motionZ)
                            ?: canMove(targetBox, 0.0, motionY, 0.0)
                            ?: break
        }

        val offsetX = targetBox.minX - entityBox.minX
        val offsetY = targetBox.minY - entityBox.minY
        val offsetZ = targetBox.minZ - entityBox.minZ
        val motion = Vec3d(offsetX, offsetY, offsetZ)
        val pos = this.pos

        return EntityPredictInfo(this, pos.add(motion), targetBox, pos, motion)
    }

    fun isMoving(): Boolean = player.input.movementForward != 0.0f || player.input.movementSideways != 0.0f

    fun sendSequencedPacket(world: ClientWorld, packetCreator: SequencedPacketCreator) {
        world.pendingUpdateManager.incrementSequence().use { pendingUpdateManager ->
            val i = pendingUpdateManager.sequence
            val packet = packetCreator.predict(i)
            playerController.updateController()
            connection.sendPacket(packet)
        }
    }

    fun autoCenter() {
        val centerPos = player.blockPos
        val y = centerPos.y.toDouble()
        var x = centerPos.x.toDouble()
        var z = centerPos.z.toDouble()
        val plusPlus = Vec3d(x + 0.5, y, z + 0.5)
        val plusMinus = Vec3d(x + 0.5, y, z - 0.5)
        val minusMinus = Vec3d(x - 0.5, y, z - 0.5)
        val minusPlus = Vec3d(x - 0.5, y, z + 0.5)
        if (getDst(plusPlus) < getDst(
                plusMinus
            ) && getDst(plusPlus) < getDst(
                minusMinus
            ) && getDst(plusPlus) < getDst(
                minusPlus
            )
        ) {
            x = centerPos.x + 0.5
            z = centerPos.z + 0.5
            centerPlayer(x, y, z)
        }
        if (getDst(plusMinus) < getDst(
                plusPlus
            ) && getDst(plusMinus) < getDst(
                minusMinus
            ) && getDst(plusMinus) < getDst(
                minusPlus
            )
        ) {
            x = centerPos.x + 0.5
            z = centerPos.z - 0.5
            centerPlayer(x, y, z)
        }
        if (getDst(minusMinus) < getDst(
                plusPlus
            ) && getDst(minusMinus) < getDst(
                plusMinus
            ) && getDst(minusMinus) < getDst(
                minusPlus
            )
        ) {
            x = centerPos.x - 0.5
            z = centerPos.z - 0.5
            centerPlayer(x, y, z)
        }
        if (getDst(minusPlus) < getDst(
                plusPlus
            ) && getDst(minusPlus) < getDst(
                plusMinus
            ) && getDst(minusPlus) < getDst(
                minusMinus
            )
        ) {
            x = centerPos.x - 0.5
            z = centerPos.z + 0.5
            centerPlayer(x, y, z)
        }
    }

    fun getDst(vec: Vec3d?): Double {
        return player.pos.distanceTo(vec)
    }

    private fun centerPlayer(x: Double, y: Double, z: Double) {
        connection.sendPacket(PlayerMoveC2SPacket.Full(x, y, z, player.yaw, player.pitch, true))
        player.setPosition(x, y, z)
    }

    fun ClientPlayerEntity.spoofSneaking(mod: () -> Unit) {
        player.isSneaking = true
        mod.invoke()
        player.isSneaking = false
    }

    fun blinkToPos(startPos: Vec3d, endPos: BlockPos, slack: Double, pOffset: DoubleArray) {
        var curX = startPos.x
        var curY = startPos.y
        var curZ = startPos.z
        try {
            val endX = endPos.x + 0.5
            val endY = endPos.y + 1.0
            val endZ = endPos.z + 0.5
            var distance = abs(curX - endX) + abs(curY - endY) + abs(curZ - endZ)
            var count = 0
            while (distance > slack) {
                distance = abs(curX - endX) + abs(curY - endY) + abs(curZ - endZ)
                if (count > 120) {
                    break
                }
                val diffX = curX - endX
                val diffY = curY - endY
                val diffZ = curZ - endZ
                val offset = if (count and 0x1 == 0x0) pOffset[0] else pOffset[1]
                if (diffX < 0.0) {
                    curX += if (abs(diffX) > offset) {
                        offset
                    } else {
                        abs(diffX)
                    }
                }
                if (diffX > 0.0) {
                    curX -= if (abs(diffX) > offset) {
                        offset
                    } else {
                        abs(diffX)
                    }
                }
                if (diffY < 0.0) {
                    curY += if (abs(diffY) > 0.25) {
                        0.25
                    } else {
                        abs(diffY)
                    }
                }
                if (diffY > 0.0) {
                    curY -= if (abs(diffY) > 0.25) {
                        0.25
                    } else {
                        abs(diffY)
                    }
                }
                if (diffZ < 0.0) {
                    curZ += if (abs(diffZ) > offset) {
                        offset
                    } else {
                        abs(diffZ)
                    }
                }
                if (diffZ > 0.0) {
                    curZ -= if (abs(diffZ) > offset) {
                        offset
                    } else {
                        abs(diffZ)
                    }
                }
                player.networkHandler.sendPacket(
                    PlayerMoveC2SPacket.PositionAndOnGround(
                        curX,
                        curY,
                        curZ,
                        true
                    )
                )
                ++count
            }
        } catch (e: Exception) {
        }
    }

    val dimension: String
        get() = when (world.registryKey.value.path) {
            "the_nether" -> "NETHER"
            "the_end" -> "END"
            else -> "OVERWORLD"
        }

    fun setSpeed(moveSpeed: Double, yVelocity: Double, pseudoYaw: Float, pseudoStrafe: Double, pseudoForward: Double) {
        var forward = pseudoForward
        var strafe = pseudoStrafe
        var yaw = pseudoYaw
        if (pseudoForward != 0.0) {
            if (pseudoStrafe > 0.0) {
                yaw = pseudoYaw + (if (pseudoForward > 0.0) -45 else 45).toFloat()
            } else if (pseudoStrafe < 0.0) {
                yaw = pseudoYaw + (if (pseudoForward > 0.0) 45 else -45).toFloat()
            }
            strafe = 0.0
            if (pseudoForward > 0.0) {
                forward = 1.0
            } else if (pseudoForward < 0.0) {
                forward = -1.0
            }
        }
        if (strafe > 0.0) {
            strafe = 1.0
        } else if (strafe < 0.0) {
            strafe = -1.0
        }
        val mx = cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val mz = sin(Math.toRadians((yaw + 90.0f).toDouble()))
        val x = forward * moveSpeed * mx + strafe * moveSpeed * mz
        val z = forward * moveSpeed * mz - strafe * moveSpeed * mx
        player.setVelocity(x, yVelocity, z)
    }

    fun teleport(endPos: Vec3d) {
        val dist = sqrt(player.squaredDistanceTo(endPos.getX(), endPos.getY(), endPos.getZ()))
        val packetDist = 5.0
        var xtp: Double
        var ytp: Double
        var ztp: Double
        if (dist > packetDist) {
            val nbPackets = (Math.round(dist / packetDist + 0.49999999999) - 1).toDouble()
            xtp = player.x
            ytp = player.y
            ztp = player.z
            var i = 1
            while (i < nbPackets) {
                val xdi = (endPos.getX() - player.x) / nbPackets
                xtp += xdi
                val zdi = (endPos.getZ() - player.z) / nbPackets
                ztp += zdi
                val ydi = (endPos.getY() - player.y) / nbPackets
                ytp += ydi
                val packet = PlayerMoveC2SPacket.PositionAndOnGround(xtp, ytp, ztp, true)
                player.networkHandler.sendPacket(packet)
                i++
            }
            player.setPosition(endPos.getX() + 0.5, endPos.getY(), endPos.getZ() + 0.5)
        } else {
            player.setPosition(endPos.getX(), endPos.getY(), endPos.getZ())
        }
    }

    fun distanceTo(entity: Entity): Double {
        return distanceTo(entity.x, entity.y, entity.z)
    }

    fun distanceTo(blockPos: BlockPos): Double {
        return distanceTo(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble())
    }

    fun distanceTo(vec3d: Vec3d): Double {
        return distanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ())
    }

    fun distanceTo(x: Double, y: Double, z: Double): Double {
        val f = (player.x - x).toFloat()
        val g = (player.y - y).toFloat()
        val h = (player.z - z).toFloat()
        return MathHelper.sqrt(f * f + g * g + h * h).toDouble()
    }

    fun distance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        val dX = x2 - x1
        val dY = y2 - y1
        val dZ = z2 - z1
        return sqrt(dX * dX + dY * dY + dZ * dZ)
    }

    fun directionSpeed(speed: Double): DoubleArray {
        var forward = player.input.movementForward
        var side = player.input.movementSideways
        var yaw = player.yaw
        if (forward != 0f) {
            if (side > 0) {
                yaw += (if (forward > 0) -45 else 45).toFloat()
            } else if (side < 0) {
                yaw += (if (forward > 0) 45 else -45).toFloat()
            }
            side = 0f

            //forward = clamp(forward, 0, 1);
            if (forward > 0) {
                forward = 1f
            } else if (forward < 0) {
                forward = -1f
            }
        }
        val sin = sin(Math.toRadians((yaw + 90).toDouble()))
        val cos = cos(Math.toRadians((yaw + 90).toDouble()))
        val posX = forward * speed * cos + side * speed * sin
        val posZ = forward * speed * sin - side * speed * cos
        return doubleArrayOf(posX, posZ)
    }

}