package dev.kura.net.mod.module.impl.movement

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMoveEvent
import dev.kura.net.utils.extension.floorToInt
import dev.kura.net.utils.world.BlockUtil.blockType
import dev.kura.net.asmimpl.IVec3d
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.block.Blocks
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object BlockClip : Module("BlockClip", Category.Movement) {

    private val pauseSneak by setting("PauseSneak", true)
    private val pauseElytra by setting("PauseElytra", true)
    private val pauseFly by setting("PauseFly", true)

    private var move = false
    private var velocity = 0.0

    init {

        eventListener<PlayerMoveEvent> {
            if (fullNullCheck()) return@eventListener

            if (pauseSneak && player.isSneaking) return@eventListener
            if (pauseElytra && player.isFallFlying) return@eventListener
            if (pauseFly && player.abilities.flying) return@eventListener

            if (player.isTouchingWater && player.isSubmergedIn(FluidTags.WATER)) return@eventListener
            if (player.isInLava && player.isSubmergedIn(FluidTags.LAVA)) return@eventListener

            val selfPos = getFillBlock() ?: return@eventListener

            if (!(selfPos.blockType() == Blocks.OBSIDIAN
                        || selfPos.blockType() == Blocks.ENDER_CHEST
                        || selfPos.blockType() == Blocks.BEDROCK)
            ) return@eventListener

            val forward = player.input.movementForward.toDouble()
            val sideways = player.input.movementSideways.toDouble()
            val yaw: Double = getYaw(forward, sideways)
            var motion = max(0.02, velocity * 0.98).also { velocity = it }
            if (velocity < 0.01) {
                motion = 0.0
            }
            if (player.hasStatusEffect(StatusEffects.SPEED)) {
                motion *= 1.2 + player.getStatusEffect(StatusEffects.SPEED)!!.amplifier.toDouble() * 0.2
            }
            if (player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                motion /= 1.2 + player.getStatusEffect(StatusEffects.SLOWNESS)!!.amplifier.toDouble() * 0.2
            }
            val x = cos(Math.toRadians(yaw + 90.0))
            val y = player.velocity.getY()
            val z = sin(Math.toRadians(yaw + 90.0))
            if (move) {
                (it.movement as IVec3d).set(motion * x, y, motion * z)
            } else {
                (it.movement as IVec3d).set(0.0, y, 0.0)
            }

        }

    }

    private fun getYaw(f: Double, s: Double): Double {
        var yaw = mc.player!!.yaw.toDouble()
        if (f > 0.0) {
            this.move = true
            yaw += if (s > 0.0) -45.0 else if (s < 0.0) 45.0 else 0.0
        } else if (f < 0.0) {
            this.move = true
            yaw += if (s > 0.0) -135.0 else if (s < 0.0) 135.0 else 180.0
        } else {
            this.move = s != 0.0
            val bl: Boolean = this.move
            yaw += if (s > 0.0) -90.0 else if (s < 0.0) 90.0 else 0.0
        }
        return yaw
    }

    private fun getFillBlock(): BlockPos? {
        val feetBlock: LinkedHashSet<BlockPos> = getFeetBlock(0)
        val collect: List<*> = feetBlock.stream().limit(1L).toList()
        return if (collect.isEmpty()) {
            null
        } else collect[0] as BlockPos?
    }

    private fun getFeetBlock(yOff: Int): LinkedHashSet<BlockPos> {

        val set = java.util.LinkedHashSet<BlockPos>()

        set.add(vec3d2BlockPos(player.pos.add(0.0, yOff.toDouble(), 0.0)))
        set.add(vec3d2BlockPos(player.pos.add(0.3, yOff.toDouble(), 0.3)))
        set.add(vec3d2BlockPos(player.pos.add(-0.3, yOff.toDouble(), 0.3)))
        set.add(vec3d2BlockPos(player.pos.add(0.3, yOff.toDouble(), -0.3)))
        set.add(vec3d2BlockPos(player.pos.add(-0.3, yOff.toDouble(), -0.3)))

        return set

    }

    private fun vec3d2BlockPos(vec: Vec3d): BlockPos {

        return BlockPos(
            vec.x.floorToInt(),
            vec.y.floorToInt(),
            vec.z.floorToInt()
        )

    }

}