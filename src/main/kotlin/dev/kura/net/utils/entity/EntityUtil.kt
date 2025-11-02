package dev.kura.net.utils.entity

import dev.kura.net.manager.impl.FriendManager
import dev.kura.net.utils.Util
import dev.kura.net.utils.extension.fastFloor
import dev.kura.net.utils.extension.sq
import dev.kura.net.utils.math.vector.VectorUtils.toBlockPos
import net.minecraft.block.Blocks
import net.minecraft.block.CobwebBlock
import net.minecraft.client.network.PendingUpdateManager
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object EntityUtil : Util() {
    fun PlayerEntity.isVanished(): Boolean {
        return !this.isAlive || FriendManager.isFriend(this) || this == player
    }

    fun isMoving(): Boolean {
        return player.sidewaysSpeed != 0f || player.upwardSpeed != 0f || player.forwardSpeed != 0f
    }

    fun Entity.getMinArmorRate(): Int {
        return this.armorItems.toList().asSequence().filter { it.isDamageable }
            .map { ((it.maxDamage - it.damage) * 100.0f / it.maxDamage.toFloat()).toInt() }.maxOrNull() ?: 0
    }

    fun getEyesPos(entity: Entity): Vec3d {
        return entity.pos.add(0.0, entity.getEyeHeight(entity.pose).toDouble(), 0.0)
    }

    fun LivingEntity.canMove(box: Box, x: Double, y: Double, z: Double): Box? {
        return box.offset(x, y, z).takeIf { !world.canCollide(this, it) }
    }

    val Entity.eyePosition get() = Vec3d(this.pos.x, this.pos.y + this.getEyeHeight(this.pose), this.pos.z)

    @JvmStatic
    val LivingEntity.scaledHealth: Float
        get() = this.health + this.absorptionAmount * (this.health / this.maxHealth)

    val jumpSpeed: Double
        get() {
            var jumpSpeed = 0.3999999463558197
            if (player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                player.getStatusEffect(StatusEffects.JUMP_BOOST)?.let {
                    val amplifier = it.amplifier.toDouble()
                    jumpSpeed += (amplifier + 1) * 0.1
                }
            }
            return jumpSpeed
        }


    fun ClientWorld.getWorldActionId(): Int {
        val pum = getUpdateManager(world)
        val p = pum.sequence
        pum.close()
        return p
    }

    fun World.getGroundPos(entity: Entity): BlockPos {
        return getGroundPos(entity.boundingBox)
    }

    fun World.getGroundPos(boundingBox: Box): BlockPos {
        val center = boundingBox.center

        val cx = center.x.fastFloor()
        val cz = center.z.fastFloor()

        var rx = cx
        var ry = Int.MIN_VALUE
        var rz = cz

        val pos = BlockPos.Mutable()

        for (x in boundingBox.minX.fastFloor()..boundingBox.maxX.fastFloor()) {
            for (z in boundingBox.minZ.fastFloor()..boundingBox.maxZ.fastFloor()) {
                for (y in (boundingBox.minY - 0.5).fastFloor() downTo -1) {
                    if (y < ry) break

                    pos.set(x, y, z)
                    val box = this.getBlockState(pos).getCollisionShape(this, pos)
                    if ((!box.isEmpty && box.boundingBox != null) && (ry == Int.MIN_VALUE || y > ry || (x - cx).sq <= (rx - cx).sq && (z - cz).sq <= (rz - cz).sq)) {
                        rx = x
                        ry = y
                        rz = z
                    }
                }
            }
        }

        return BlockPos(rx, if (ry == Int.MIN_VALUE) -999 else ry, rz)
    }

    fun getUpdateManager(world: ClientWorld): PendingUpdateManager {
        return world.pendingUpdateManager
    }

    val LivingEntity.totalHealth
        get() = this.health + this.absorptionAmount

    fun BlockPos.aroundBlock(radius: Int): List<BlockPos> {
        val result = mutableListOf<BlockPos>()
        for (x in -radius..radius) for (y in -radius..radius) for (z in -radius..radius) {
            result.add(this.add(x, y, z))
        }
        return result
    }

    val Entity.preventEntitySpawning get() = this !is ItemEntity

    fun isInBurrow(): Boolean {
        return (isBurrowBlock(player.blockPos) || isBurrowBlock(
            player.pos.add(0.3, 0.0, 0.3).toBlockPos()
        ) || isBurrowBlock(player.pos.add(-0.3, 0.0, 0.3).toBlockPos()) || isBurrowBlock(
            player.pos.add(
                -0.3,
                0.0,
                -0.3
            ).toBlockPos()
        ) || isBurrowBlock(player.pos.add(0.3, 0.0, -0.3).toBlockPos()))
    }

    private fun isBurrowBlock(pos: BlockPos): Boolean {
        return (world.getBlockState(pos).block == Blocks.OBSIDIAN || world.getBlockState(pos).block == Blocks.CRYING_OBSIDIAN) && player.boundingBox.intersects(
            Box(pos)
        )
    }

    fun isInWeb(entity: Entity): Boolean {
        return (isWeb(entity, player.blockPos) || isWeb(
            entity,
            entity.pos.add(0.3, 0.0, 0.3).toBlockPos()
        ) || isWeb(entity, entity.pos.add(-0.3, 0.0, 0.3).toBlockPos()) || isWeb(
            entity,
            entity.pos.add(
                -0.3,
                0.0,
                -0.3
            ).toBlockPos()
        ) || isWeb(entity, entity.pos.add(0.3, 0.0, -0.3).toBlockPos()) || isWeb(entity, player.blockPos) || isWeb(
            entity,
            entity.pos.add(0.3, 1.0, 0.3).toBlockPos()
        ) || isWeb(entity, entity.pos.add(-0.3, 1.0, 0.3).toBlockPos()) || isWeb(
            entity,
            entity.pos.add(
                -0.3,
                1.0,
                -0.3
            ).toBlockPos()
        ) || isWeb(entity, entity.pos.add(0.3, 1.0, -0.3).toBlockPos()))
    }

    private fun isWeb(entity: Entity, pos: BlockPos): Boolean {
        return world.getBlockState(pos).block is CobwebBlock && entity.boundingBox.intersects(
            Box(pos)
        )
    }

    fun boxCheck(box: Box, itemCheck: Boolean = false): Boolean {
        return world.entities.none {
            (it !is ItemEntity || !itemCheck);it.isAlive;it.boundingBox.intersects(box)
        }
    }

    fun canMove(box: Box, x: Double, y: Double, z: Double): Box? {
        synchronized(this) {
            runCatching {
                return box.offset(x, y, z).takeIf { world.noCollision(it.center.toBlockPos()) }
            }
            return null
        }
    }

    fun World.noCollision(pos: BlockPos) = this.isSpaceEmpty(player, Box(pos))
}