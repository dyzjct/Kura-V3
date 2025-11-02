package dev.m7thh4ck.net.mod.module.impl.misc

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.TickEvent
import dev.m7thh4ck.net.util.extension.sq
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object BaseFinder : Module("BaseFinder", Category.Misc) {

    private val range by setting("Range", 16, 16..16384, 16)

    private var lastRotatePos = BlockPos(0, 0, 0)
    private var dir = Direction.NORTH
    private var rotateRange = 0

    init {

        onEnable {
            if (fullNullCheck()) return@onEnable
            lastRotatePos = player.blockPos
            dir = Direction.NORTH
            rotateRange = range
        }

        onDisable {
            if (fullNullCheck()) return@onDisable
            mc.options.forwardKey.isPressed = false
        }

        eventListener<TickEvent.Post> {
            if (fullNullCheck()) return@eventListener
            mc.options.forwardKey.isPressed = true

            if (player.squaredDistanceTo(lastRotatePos.toCenterPos()) > rotateRange.sq) {
                dir = when (dir) {
                    Direction.NORTH -> Direction.EAST
                    Direction.EAST -> Direction.SOUTH
                    Direction.SOUTH -> Direction.WEST
                    Direction.WEST -> Direction.NORTH
                    else -> Direction.NORTH
                }

                lastRotatePos = player.blockPos
                rotateRange += range
            }

            if (player.yaw != dir.asRotation()) {
                player.yaw = dir.asRotation()
            }

        }

    }

}