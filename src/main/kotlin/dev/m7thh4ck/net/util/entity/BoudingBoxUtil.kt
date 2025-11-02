package dev.m7thh4ck.net.util.entity

import dev.m7thh4ck.net.util.Wrapper.player
import dev.m7thh4ck.net.util.entity.EntityUtil.eyePosition
import dev.m7thh4ck.net.util.math.vector.VectorUtils.plus
import dev.m7thh4ck.net.util.math.vector.VectorUtils.times
import dev.m7thh4ck.net.util.math.vector.VectorUtils.toBlockPos
import dev.m7thh4ck.net.util.math.vector.VectorUtils.toVec3d
import dev.m7thh4ck.net.util.math.vector.VectorUtils.toViewVec
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import kotlin.math.min

inline val Box.xCenter get() = minX + xLength * 0.5

inline val Box.yCenter get() = minY + yLength * 0.5

inline val Box.zCenter get() = minZ + zLength * 0.5

inline val Box.xLength get() = maxX - minX

inline val Box.yLength get() = maxY - minY

inline val Box.zLength get() = maxY - minY

inline val Box.lengths get() = Vec3d(xLength, yLength, zLength)

fun Box.corners(scale: Double): Array<Vec3d> {
    val growSizes = lengths * (scale - 1.0)
    return expand(growSizes.x, growSizes.y, growSizes.z).corners()
}

fun Box.corners() = arrayOf(
    Vec3d(minX, minY, minZ),
    Vec3d(minX, minY, maxZ),
    Vec3d(minX, maxY, minZ),
    Vec3d(minX, maxY, maxZ),
    Vec3d(maxX, minY, minZ),
    Vec3d(maxX, minY, maxZ),
    Vec3d(maxX, maxY, minZ),
    Vec3d(maxX, maxY, maxZ),
)

fun Box.side(side: Direction, scale: Double = 0.5): Vec3d {
    val lengths = lengths
    val sideDirectionVec = side.vector.toVec3d()
    return lengths * sideDirectionVec * scale + center
}

fun Box.scale(multiplier: Double): Box {
    return this.scale(multiplier, multiplier, multiplier)
}

fun Box.scale(x: Double, y: Double, z: Double): Box {
    val halfXLength = this.xLength * 0.5
    val halfYLength = this.yLength * 0.5
    val halfZLength = this.zLength * 0.5

    return this.expand(halfXLength * (x - 1.0), halfYLength * (y - 1.0), halfZLength * (z - 1.0))
}

fun Box.scale(multiplier: Float): Box {
    return this.scale(multiplier, multiplier, multiplier)
}

fun Box.scale(x: Float, y: Float, z: Float): Box {
    val halfXLength = this.xLength * 0.5f
    val halfYLength = this.yLength * 0.5f
    val halfZLength = this.zLength * 0.5f

    return this.expand(halfXLength * (x - 1f), halfYLength * (y - 1f), halfZLength * (z - 1f))
}

fun Box.limitSize(x: Double, y: Double, z: Double): Box {
    val halfX = min(xLength, x) / 2.0
    val halfY = min(yLength, y) / 2.0
    val halfZ = min(zLength, z) / 2.0
    val center = center

    return Box(
        center.x - halfX, center.y - halfY, center.z - halfZ,
        center.x + halfX, center.y + halfY, center.z + halfZ,
    )
}

fun Box.isInSight(
    posFrom: Vec3d = player.eyePosition,
    rotation: Vec2f = player.let { Vec2f(it.yaw, it.pitch) },
    range: Double = 4.25,
    tolerance: Double = 1.1
) = isInSight(posFrom, rotation.toViewVec(), range, tolerance)

fun Box.isInSight(
    posFrom: Vec3d,
    viewVec: Vec3d,
    range: Double,
    tolerance: Double
): BlockHitResult? {
    val sightEnd = posFrom.add(viewVec.multiply(range))
    val startVec = Vec3d(posFrom.x, posFrom.y, posFrom.z)
    val endVec = Vec3d(sightEnd.x, sightEnd.y, sightEnd.z)
    val finalVec = expand(tolerance).raycast(startVec, endVec)
    return if (!finalVec.isEmpty) {
        BlockHitResult(
            finalVec.get(),
            player.movementDirection,
            posFrom.toBlockPos(),
            false
        )
    } else {
        null
    }
}