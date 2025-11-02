package dev.m7thh4ck.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.util.helper.ChatUtil
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.abs

object HitboxDesync : Module("CrystalClip", Category.Player) {
    private const val MAGIC_OFFSET = 0.200009968835369999878673424677777777777761

    init {
        eventListener<TickEvent.Post> {
            val f = player.horizontalFacing
            val bb = player.boundingBox
            val center = bb.center
            val offset = Vec3d(f.unitVector)



            val fin: Vec3d = merge(
                Vec3d.of(BlockPos.ofFloored(center)).add(.5, 0.0, .5)
                    .add(offset.multiply(MAGIC_OFFSET)), f
            )
            player.setPosition(
                if (fin.x == 0.0) player.x else fin.x,
                player.y,
                if (fin.z == 0.0) player.z else fin.z
            )
            toggle()
            ChatUtil.sendRawMessage("Clip")
        }
    }

    private fun merge(a: Vec3d, facing: Direction): Vec3d {
        return Vec3d(
            a.x * abs(facing.unitVector.x()),
            a.y * abs(facing.unitVector.y()),
            a.z * abs(facing.unitVector.z())
        )
    }
}

