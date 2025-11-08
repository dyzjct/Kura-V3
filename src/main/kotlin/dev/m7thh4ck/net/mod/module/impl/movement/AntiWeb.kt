package dev.m7thh4ck.net.mod.module.impl.movement

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.PlayerUpdateEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.combat.AutoWeb.setting
import dev.m7thh4ck.net.mod.module.impl.movement.Step.setting
import dev.m7thh4ck.net.util.world.BlockUtil.forward
import net.minecraft.block.Blocks
import net.minecraft.block.CobwebBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

object AntiWeb : Module("AntiWeb", Category.Movement) {
    private val speed by setting("Height", 0.3, 0.0..10.0, 0.1)
    private val setVel by setting("setVel", 0.0, 0.0..10.0, 0.1)
    private var sneak by setting("sneak", false)
    init {
        eventListener<PlayerUpdateEvent> {
            if (isInWeb()) {

               val dir: DoubleArray = forward(speed)
               player.setVelocity(dir[0], 0.0 + setVel, dir[1])

                if (mc.options.jumpKey.isPressed) player.velocity =
                        player.velocity.add(0.0, speed, 0.0)

                if(sneak) {
                    if (mc.options.sneakKey.isPressed) player.velocity =
                        player.velocity.add(0.0, -speed, 0.0)
                }


            }
        }
    }


    fun isInWeb(): Boolean {
        val pBox = player.boundingBox
        val pBlockPos = BlockPos.ofFloored(player.getPos())
        for (x in pBlockPos.x - 2..pBlockPos.x + 2) {
            for (y in pBlockPos.y - 1..pBlockPos.y + 4) {
                for (z in pBlockPos.z - 2..pBlockPos.z + 2) {
                    val bp = BlockPos(x, y, z)
                    if (pBox.intersects(Box(bp)) && world.getBlockState(bp).block === Blocks.COBWEB) return true
                }
            }
        }
        return false
    }


}