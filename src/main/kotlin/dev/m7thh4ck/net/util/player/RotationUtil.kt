package dev.m7thh4ck.net.util.player

import dev.m7thh4ck.net.util.Wrapper.player
import net.minecraft.entity.Entity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

object RotationUtil {
    fun canSee(entity: Entity): Boolean {
        val entityEyes = getEyesPos(entity)
        val entityPos = entity.pos
        return canSee(entityEyes, entityPos)
    }

    fun canSee(entityEyes: Vec3d?, entityPos: Vec3d): Boolean {
        if (InteractionUtil.mc.player == null || InteractionUtil.mc.world == null) return false
        val playerEyes = getEyesPos(InteractionUtil.mc.player)
        if (InteractionUtil.mc.world!!.raycast(
                RaycastContext(
                    playerEyes,
                    entityEyes,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    InteractionUtil.mc.player
                )
            ).type === HitResult.Type.MISS
        ) return true
        return if (playerEyes.getY() > entityPos.getY()) InteractionUtil.mc.world!!.raycast(
            RaycastContext(
                playerEyes,
                entityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                InteractionUtil.mc.player
            )
        ).type === HitResult.Type.MISS else false
    }


    fun getEyesPos(entity: Entity?): Vec3d {
        return entity!!.pos.add(0.0, entity.getEyeHeight(entity.pose).toDouble(), 0.0)
    }

    fun calculateAngle(to: Vec3d): FloatArray {
        return calculateAngle(getEyesPos(player), to)
    }

    fun calculateAngle(from: Vec3d, to: Vec3d): FloatArray {
        val difX = to.x - from.x
        val difY = (to.y - from.y) * -1.0
        val difZ = to.z - from.z
        val dist = MathHelper.sqrt((difX * difX + difZ * difZ).toFloat()).toDouble()
        val yD = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0).toFloat()
        val pD = MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))), -90.0, 90.0).toFloat()
        return floatArrayOf(yD, pD)
    }


    fun getPlaceAngle(bp: BlockPos, interact: InteractionUtil.Interact, ignoreEntities: Boolean): FloatArray? {
        val result = InteractionUtil.getPlaceResult(bp, interact, ignoreEntities)
        return if (result != null) calculateAngle(result.pos) else null
    }

    fun squaredDistanceFromEyes(vec: Vec3d): Float {
        val d0 = vec.x - InteractionUtil.mc.player!!.x
        val d1 = vec.z - InteractionUtil.mc.player!!.z
        val d2 = vec.y - (InteractionUtil.mc.player!!.y + InteractionUtil.mc.player!!.getEyeHeight(InteractionUtil.mc.player!!.pose))
        return (d0 * d0 + d1 * d1 + d2 * d2).toFloat()
    }

    fun getStrictDirections(bp: BlockPos): List<Direction> {
        val visibleSides: MutableList<Direction> = ArrayList()
        val positionVector = bp.toCenterPos()
        val westDelta = getEyesPos(InteractionUtil.mc.player).x - positionVector.add(0.5, 0.0, 0.0).x
        val eastDelta = getEyesPos(InteractionUtil.mc.player).x - positionVector.add(-0.5, 0.0, 0.0).x
        val northDelta = getEyesPos(InteractionUtil.mc.player).z - positionVector.add(0.0, 0.0, 0.5).z
        val southDelta = getEyesPos(InteractionUtil.mc.player).z - positionVector.add(0.0, 0.0, -0.5).z
        val upDelta = getEyesPos(InteractionUtil.mc.player).y - positionVector.add(0.0, 0.5, 0.0).y
        val downDelta = getEyesPos(InteractionUtil.mc.player).y - positionVector.add(0.0, -0.5, 0.0).y
        if (westDelta > 0 && !InteractionUtil.mc.world!!.getBlockState(bp.west()).isReplaceable) visibleSides.add(
            Direction.EAST)
        if (westDelta < 0 && !InteractionUtil.mc.world!!.getBlockState(bp.east()).isReplaceable) visibleSides.add(
            Direction.WEST)
        if (eastDelta < 0 && !InteractionUtil.mc.world!!.getBlockState(bp.east()).isReplaceable) visibleSides.add(
            Direction.WEST)
        if (eastDelta > 0 && !InteractionUtil.mc.world!!.getBlockState(bp.west()).isReplaceable) visibleSides.add(
            Direction.EAST)
        if (northDelta > 0 && !InteractionUtil.mc.world!!.getBlockState(bp.north()).isReplaceable) visibleSides.add(
            Direction.SOUTH)
        if (northDelta < 0 && !InteractionUtil.mc.world!!.getBlockState(bp.south()).isReplaceable) visibleSides.add(
            Direction.NORTH)
        if (southDelta < 0 && !InteractionUtil.mc.world!!.getBlockState(bp.south()).isReplaceable) visibleSides.add(
            Direction.NORTH)
        if (southDelta > 0 && !InteractionUtil.mc.world!!.getBlockState(bp.north()).isReplaceable) visibleSides.add(
            Direction.SOUTH)
        if (upDelta > 0 && !InteractionUtil.mc.world!!.getBlockState(bp.down()).isReplaceable) visibleSides.add(
            Direction.UP)
        if (upDelta < 0 && !InteractionUtil.mc.world!!.getBlockState(bp.up()).isReplaceable) visibleSides.add(Direction.DOWN)
        if (downDelta < 0 && !InteractionUtil.mc.world!!.getBlockState(bp.up()).isReplaceable) visibleSides.add(
            Direction.DOWN)
        if (downDelta > 0 && !InteractionUtil.mc.world!!.getBlockState(bp.down()).isReplaceable) visibleSides.add(
            Direction.UP)
        return visibleSides
    }
}