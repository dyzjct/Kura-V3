package dev.kura.net.utils.world

import dev.kura.net.manager.impl.RotationManager
import dev.kura.net.mod.module.impl.combat.SelfFill.rotate
import dev.kura.net.mod.module.impl.player.PacketMine.Tick
import dev.kura.net.mod.module.impl.player.PacketMine.disable
import dev.kura.net.mod.module.impl.player.PacketMine.relative
import dev.kura.net.mod.module.impl.player.PacketMine.swing
import dev.kura.net.mod.module.impl.render.PlaceRender
import dev.kura.net.utils.Util
import dev.kura.net.utils.entity.EntityUtil.eyePosition
import dev.kura.net.utils.entity.EntityUtil.noCollision
import dev.kura.net.utils.extension.fastCeil
import dev.kura.net.utils.extension.sq
import dev.kura.net.utils.item.isTool
import dev.kura.net.utils.math.vector.Vec3f
import dev.kura.net.utils.math.vector.VectorUtils.toBlockPos
import dev.kura.net.utils.math.vector.VectorUtils.toVec3dCenter
import dev.kura.net.utils.other.EnumSet
import dev.kura.net.utils.player.InteractionUtil
import dev.kura.net.utils.player.InventoryUtil
import dev.kura.net.utils.player.RotationUtil
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.*
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import java.util.*

object BlockUtil : Util() {

    fun BlockPos.blockType(): Block = world.getBlockState(this).block

    @JvmStatic
    fun canBreak(pos: BlockPos): Boolean {
        when (pos.blockType()) {
            Blocks.BEDROCK -> return false
            Blocks.END_PORTAL_FRAME -> return false
            Blocks.END_PORTAL -> return false
            Blocks.WATER -> return false
            Blocks.LAVA -> return false
        }
        return world.getBlockState(pos).getHardness(world, pos) != 1.0f
    }

    fun DownPlace(){
        val obsSlot = InventoryUtil.findItemInHotbar(Items.OBSIDIAN)
        val eChestSlot = InventoryUtil.findItemInHotbar(Items.ENDER_CHEST)

        if (obsSlot == null && eChestSlot == null) {
            disable()
            return
        }
        if (rotate) {
            RotationManager.addRotation(player.yaw, 90f, 0)
        }
        if (world.isAir(player.blockPos.down())) {
            InventoryUtil.spoofHotbar(obsSlot ?: eChestSlot ?: -1) {
                InteractionUtil.placeBlock(
                    player.blockPos.down(), InteractionUtil.Interact.Legit,
                    InteractionUtil.PlaceMode.Packet, true
                )
            }
            swing()
        }
        val pos5 = BlockPos(
            MathHelper.floor(player.x + 0.3), MathHelper.floor(player.y - 0.5), MathHelper.floor(
                player.z + 0.3
            )
        )
        val pos6 = BlockPos(
            MathHelper.floor(player.x - 0.3), MathHelper.floor(player.y - 0.5), MathHelper.floor(
                player.z + 0.3
            )
        )
        val pos7 = BlockPos(
            MathHelper.floor(player.x + 0.3), MathHelper.floor(player.y - 0.5), MathHelper.floor(
                player.z - 0.3
            )
        )
        val pos8 = BlockPos(
            MathHelper.floor(player.x - 0.3), MathHelper.floor(player.y - 0.5), MathHelper.floor(
                player.z - 0.3
            )
        )

        if (world.isAir(pos5)) {
            InventoryUtil.spoofHotbar(obsSlot ?: eChestSlot ?: -1) {
                InteractionUtil.placeBlock(
                    pos5, InteractionUtil.Interact.Legit,
                    InteractionUtil.PlaceMode.Packet, true
                )
            }
            swing()
        }
        if (world.isAir(pos6)) {
            InventoryUtil.spoofHotbar(obsSlot ?: eChestSlot ?: -1) {
                InteractionUtil.placeBlock(
                    pos6, InteractionUtil.Interact.Legit,
                    InteractionUtil.PlaceMode.Packet, true
                )
            }
            swing()
        }
        if (world.isAir(pos7)) {
            InventoryUtil.spoofHotbar(obsSlot ?: eChestSlot ?: -1) {
                InteractionUtil.placeBlock(
                    pos7, InteractionUtil.Interact.Legit,
                    InteractionUtil.PlaceMode.Packet, true
                )
            }
            swing()
        }
        if (world.isAir(pos8)) {
            InventoryUtil.spoofHotbar(obsSlot ?: eChestSlot ?: -1) {
                InteractionUtil.placeBlock(
                    pos8, InteractionUtil.Interact.Legit,
                    InteractionUtil.PlaceMode.Packet, true
                )
            }
            swing()
        }
    }
    fun forward(d: Double): DoubleArray {
        var f = player.input.movementForward
        var f2 = player.input.movementSideways
        var f3 = player.getYaw()
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
        val d2 = Math.sin(Math.toRadians((f3 + 90.0f).toDouble()))
        val d3 = Math.cos(Math.toRadians((f3 + 90.0f).toDouble()))
        val d4 = f * d * d3 + f2 * d * d2
        val d5 = f * d * d2 - f2 * d * d3
        return doubleArrayOf(d4, d5)
    }
    @JvmStatic
    fun calcBreakTime(pos: BlockPos, inventory: Boolean): Float {
        val blockState = world.getBlockState(pos)

        val hardness = blockState.getHardness(world, pos)
        val breakSpeed = getBreakSpeed(blockState, inventory)

        if (breakSpeed == -1.0f) {
            return -1f
        }

        val relativeDamage = breakSpeed / hardness / relative
        val ticks = (0.7f / relativeDamage).fastCeil()
        return ticks * Tick
    }

    @JvmStatic
    fun getBreakSpeed(blockState: BlockState, inventory: Boolean = false): Float {
        var maxSpeed = 1.0f
        for (slot in 0..9) {
            val stack = player.inventory.getStack(slot)
            if (stack.isEmpty || !stack.item.isTool) {
                continue
            } else {
                var speed = stack.getMiningSpeedMultiplier(blockState)

                if (speed <= 1.0f) {
                    continue
                } else {
                    val efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack)
                    if (efficiency > 0) {
                        speed += efficiency * efficiency + 1.0f
                    }
                }

                if (speed > maxSpeed) {
                    maxSpeed = speed
                }
            }
        }

        return maxSpeed
    }

    fun fastPos(
        pos: BlockPos,
        strictDirection: Boolean = false,
        render: Boolean = true,
        face: Direction = Direction.UP,
        hand: Hand = Hand.MAIN_HAND,
        inside: Boolean = false,
        sequence: Int = 0
    ): PlayerInteractBlockC2SPacket {
        val placePos = getNeighbor(pos, strictDirection) ?: EasyBlock(pos, face)
        if (render) PlaceRender.renderBlocks[pos] = System.currentTimeMillis()
        return PlayerInteractBlockC2SPacket(
            hand,
            BlockHitResult(
                pos.toCenterPos(),
                placePos.face,
                placePos.blockPos,
                inside
            ),
            sequence
        )
    }

    fun fastPos(
        vec: Vec3d,
        strictDirection: Boolean = false,
        render: Boolean = true,
        face: Direction = Direction.UP,
        hand: Hand = Hand.MAIN_HAND,
        inside: Boolean = false,
        sequence: Int = 0
    ): PlayerInteractBlockC2SPacket {
        val placePos = getNeighbor(vec.toBlockPos(), strictDirection) ?: EasyBlock(vec.toBlockPos(), face)
        if (render) PlaceRender.renderBlocks[vec.toBlockPos()] = System.currentTimeMillis()
        return PlayerInteractBlockC2SPacket(
            hand,
            BlockHitResult(
                vec,
                placePos.face,
                placePos.blockPos,
                inside
            ),
            sequence
        )
    }

    fun getNeighbor(pos: BlockPos, strictDirection: Boolean): EasyBlock? {
        for (side in Direction.values()) {
            val offsetPos = pos.offset(side)
            val oppositeSide = side.opposite

            if (strictDirection && !getVisibleSides(offsetPos, true).contains(oppositeSide)) continue
            if (world.getBlockState(offsetPos).isReplaceable) continue

            return EasyBlock(offsetPos, oppositeSide ?: Direction.UP)
        }

        return null
    }

    fun rayCastBlock(context: RaycastContext, block: BlockPos): BlockHitResult {
        return BlockView.raycast(context.start, context.end, context, { raycastContext, blockPos ->
            val blockState: BlockState = if (!blockPos.equals(block)) {
                Blocks.AIR.defaultState
            } else {
                Blocks.OBSIDIAN.defaultState
            }
            val vec3d: Vec3d = raycastContext.start
            val vec3d2: Vec3d = raycastContext.end
            val voxelShape: VoxelShape = raycastContext.getBlockShape(blockState, world, blockPos)
            val blockHitResult = world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState)
            val voxelShape2 = VoxelShapes.empty()
            val blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos)
            val d = if (blockHitResult == null) Double.MAX_VALUE else raycastContext.start
                .squaredDistanceTo(blockHitResult.pos)
            val e = if (blockHitResult2 == null) Double.MAX_VALUE else {
                raycastContext.start
                    .squaredDistanceTo(blockHitResult2.pos)
            }
            if (d <= e) blockHitResult else blockHitResult2
        }) { raycastContext ->
            val vec3d: Vec3d = raycastContext.start.subtract(raycastContext.end)
            var bestFace = Direction.UP
            for (facing in Direction.entries) {
                if (RotationUtil.getStrictDirections(block).isEmpty()) break
                if (!RotationUtil.getStrictDirections(block).contains(facing)) continue
                bestFace = facing
            }
            BlockHitResult.createMissed(
                raycastContext.end,
                bestFace,
                BlockPos.ofFloored(raycastContext.end)
            )
        }
    }

    fun canBlockFacing(pos: BlockPos): Boolean = getPlaceSide(pos) != null

    fun getPlaceSide(pos: BlockPos): Direction? {
        var side: Direction? = null
        for (i in Direction.entries) {
            if (world.getBlockState(pos.offset(i)).isSolid) {
                side = i
                break
            }
        }
        return side
    }

    fun getMiningSide(pos: BlockPos): Direction? {
        val eyePos = player.eyePosition

        return getVisibleSides(pos)
            .filter { !world.getBlockState(pos.offset(it)).isFullBox }
            .minByOrNull { eyePos.squaredDistanceTo(getHitVec(pos, it)) }
    }

    fun getClosestVisibleSide(pos: BlockPos): Direction? {
        val eyePos = player.eyePosition

        return getVisibleSides(pos)
            .minByOrNull { eyePos.squaredDistanceTo(getHitVec(pos, it)) }
    }

    /**
     * Get the "visible" sides related to player's eye position
     */
    fun getVisibleSides(pos: BlockPos, assumeAirAsFullBox: Boolean = false): Set<Direction> {
        val visibleSides = EnumSet<Direction>()

        val eyePos = player.eyePosition
        val blockCenter = pos.toVec3dCenter()
        val blockState = world.getBlockState(pos)
        val isFullBox = assumeAirAsFullBox && blockState.block == Blocks.AIR || blockState.isFullBox

        return visibleSides
            .checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, !isFullBox)
            .checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true)
            .checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, !isFullBox)
    }

    private fun EnumSet<Direction>.checkAxis(
        diff: Double,
        negativeSide: Direction,
        positiveSide: Direction,
        bothIfInRange: Boolean
    ) =
        this.apply {
            when {
                diff < -0.5 -> {
                    add(negativeSide)
                }

                diff > 0.5 -> {
                    add(positiveSide)
                }

                else -> {
                    if (bothIfInRange) {
                        add(negativeSide)
                        add(positiveSide)
                    }
                }
            }
        }

    fun getHitVecOffset(facing: Direction): Vec3f {
        val vec = facing.vector
        return Vec3f(vec.x * 0.5f + 0.5f, vec.y * 0.5f + 0.5f, vec.z * 0.5f + 0.5f)
    }

    inline val BlockState.isFullBox: Boolean
        get() = world.let {
            if (!getCollisionShape(it, BlockPos.ORIGIN).isEmpty) {
                this.getOutlineShape(it, BlockPos.ORIGIN).boundingBox
            } else {
                false
            }
        } == Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)

    fun getHitVec(pos: BlockPos, facing: Direction): Vec3d {
        val vec = facing.vector
        return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
    }

    fun getNeighborSequence(
        pos: BlockPos,
        attempts: Int = 3,
        range: Float = 4.25f,
        visibleSideCheck: Boolean = false,
        entityCheck: Boolean = true,
        sides: Array<Direction> = Direction.values()
    ) =
        getNeighborSequence(
            player.eyePosition,
            pos,
            attempts,
            range,
            visibleSideCheck,
            entityCheck,
            sides,
            ArrayList(),
            pos,
            0
        )


    private fun getNeighborSequence(
        eyePos: Vec3d,
        pos: BlockPos,
        attempts: Int,
        range: Float,
        visibleSideCheck: Boolean,
        entityCheck: Boolean,
        sides: Array<Direction>,
        sequence: ArrayList<PlaceInfo>,
        origin: BlockPos,
        lastDist: Int
    ): List<PlaceInfo>? {
        for (side in sides) {
            checkNeighbor(eyePos, pos, side, range, visibleSideCheck, entityCheck, true, origin, lastDist)?.let {
                sequence.add(it)
                sequence.reverse()
                return sequence
            }
        }

        if (attempts > 1) {
            for (side in sides) {
                val newPos = pos.offset(side)

                val placeInfo =
                    checkNeighbor(eyePos, pos, side, range, visibleSideCheck, entityCheck, false, origin, lastDist)
                        ?: continue
                val newSequence = ArrayList(sequence)
                newSequence.add(placeInfo)

                return getNeighborSequence(
                    eyePos,
                    newPos,
                    attempts - 1,
                    range,
                    visibleSideCheck,
                    entityCheck,
                    sides,
                    newSequence,
                    origin,
                    lastDist + 1
                )
                    ?: continue
            }
        }

        return null
    }

    fun getNeighbor(
        pos: BlockPos,
        attempts: Int = 3,
        range: Float = 4.25f,
        visibleSideCheck: Boolean = false,
        entityCheck: Boolean = true,
        sides: Array<Direction> = Direction.values()
    ) =
        getNeighbor(player.eyePosition, pos, attempts, range, visibleSideCheck, entityCheck, sides, pos, 0)

    private fun getNeighbor(
        eyePos: Vec3d,
        pos: BlockPos,
        attempts: Int,
        range: Float,
        visibleSideCheck: Boolean,
        entityCheck: Boolean,
        sides: Array<Direction>,
        origin: BlockPos,
        lastDist: Int
    ): PlaceInfo? {
        for (side in sides) {
            val result = checkNeighbor(eyePos, pos, side, range, visibleSideCheck, entityCheck, true, origin, lastDist)
            if (result != null) return result
        }

        if (attempts > 1) {
            for (side in sides) {
                val newPos = pos.offset(side)
                if (!world.isPlaceable(newPos)) continue

                return getNeighbor(
                    eyePos,
                    newPos,
                    attempts - 1,
                    range,
                    visibleSideCheck,
                    entityCheck,
                    sides,
                    origin,
                    lastDist + 1
                )
                    ?: continue
            }
        }

        return null
    }

    private fun checkNeighbor(
        eyePos: Vec3d,
        pos: BlockPos,
        side: Direction,
        range: Float,
        visibleSideCheck: Boolean,
        entityCheck: Boolean,
        checkReplaceable: Boolean,
        origin: BlockPos,
        lastDist: Int
    ): PlaceInfo? {
        val offsetPos = pos.offset(side)
        val oppositeSide = side.opposite

        val distToOrigin = (offsetPos.x - origin.x).sq + (offsetPos.y - origin.y).sq + (offsetPos.z - origin.z).sq
        if (distToOrigin <= lastDist.sq) return null

        val hitVec = getHitVec(offsetPos, oppositeSide)
        val dist = eyePos.distanceTo(hitVec)

        if (dist > range) return null
        if (visibleSideCheck && !getVisibleSides(offsetPos, true).contains(oppositeSide)) return null
        if (checkReplaceable && world.getBlockState(offsetPos).isReplaceable) return null
        if (!world.getBlockState(pos).isReplaceable) return null
        if (entityCheck && !world.noCollision(pos)) return null

        val hitVecOffset = getHitVecOffset(oppositeSide)
        return PlaceInfo(offsetPos, oppositeSide, dist, hitVecOffset, hitVec, pos)
    }

    fun World.isPlaceable(pos: BlockPos, ignoreSelfCollide: Boolean = false) =
        this.getBlockState(pos).isReplaceable
                && this.isSpaceEmpty(if (ignoreSelfCollide) player else null, Box(pos))

    class EasyBlock(var blockPos: BlockPos, var face: Direction)
}