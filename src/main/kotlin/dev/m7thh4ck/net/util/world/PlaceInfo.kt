package dev.m7thh4ck.net.util.world

import dev.m7thh4ck.net.util.Wrapper.player
import dev.m7thh4ck.net.util.entity.EntityUtil.eyePosition
import dev.m7thh4ck.net.util.math.vector.Vec3f
import dev.m7thh4ck.net.util.world.BlockUtil.getHitVec
import dev.m7thh4ck.net.util.world.BlockUtil.getHitVecOffset
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class PlaceInfo(
    val pos: BlockPos,
    val side: Direction,
    val dist: Double,
    val hitVecOffset: Vec3f,
    val hitVec: Vec3d,
    val placedPos: BlockPos
) {
    companion object {
        fun newPlaceInfo(pos: BlockPos, side: Direction): PlaceInfo {
            val hitVecOffset = getHitVecOffset(side)
            val hitVec = getHitVec(pos, side)

            return PlaceInfo(pos, side, player.eyePosition.distanceTo(hitVec), hitVecOffset, hitVec, pos.offset(side))
        }
    }
}