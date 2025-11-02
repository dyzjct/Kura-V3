package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMotionEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.combat.HolePush.doHolePush
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine.hookPos
import dev.m7thh4ck.net.util.player.getTarget
import dev.m7thh4ck.net.util.world.BlockUtil.canBreak
import dev.m7thh4ck.net.util.world.BlockUtil.getMiningSide
import net.minecraft.block.Blocks
import net.minecraft.block.RedstoneBlock
import net.minecraft.util.math.BlockPos

object CityMiner : Module(name = "CityMiner", category = Category.Combat) {
    private val range by setting("Range", 6, 1..6, 1)
    private var eatingPause by setting("EatingPause", false)
    private var raytrace by setting("RayTrace", false)
    private var onlyOne by setting("OnlyOne", false)
    private var clickedPos: BlockPos? = null
    private var inTask = false

    init {
        eventListener<PlayerMotionEvent> {
            if (inTask && clickedPos != null) {
                if (world.isAir(clickedPos) || PacketMine.blockData?.blockPos != clickedPos) inTask = false
            }
            if (eatingPause && player.isUsingItem) return@eventListener
            val target = getTarget(range.toDouble())
            if (target == null) {
                inTask = false
                clickedPos = null
                return@eventListener
            }
    //        target.move(MovementType.SELF, 0,0,0)
            val targetPos = target.blockPos ?: return@eventListener
            if (!player.isOnGround) return@eventListener
            if (HolePush.isEnabled()) {
                PacketMine.blockData?.let { data ->
                    doHolePush(targetPos.up(1), true, null, null)?.let { stonePos ->
                        if (stonePos == data.blockPos) return@eventListener
                    }
                    if (world.getBlockState(data.blockPos).block is RedstoneBlock) return@eventListener
                }
            }
            for (offset in SurroundOffset.entries) {
                if (inTask) break
                val offsetPos = targetPos.add(offset.offset)
                if (!canBreak(offsetPos)) continue
                if (world.isAir(offsetPos)) continue
                if (raytrace && getMiningSide(offsetPos) == null) continue
                if (onlyOne) {
                    //Instant mine
                    PacketMine.blockData?.let {
                        if (world.isAir(it.blockPos)) {
                            for (offsetCheck in SurroundOffset.entries) {
                                if (it.blockPos == targetPos.add(offsetCheck.offset)) return@eventListener
                            }
                        }
                    }
                }
                if (world.getBlockState(offsetPos).block == Blocks.COBWEB) continue
                hookPos(offsetPos)
                clickedPos = offsetPos
                inTask = true
                break
            }
        }
        onEnable {
            inTask = false
            clickedPos = null
        }
    }

    private enum class SurroundOffset(val offset: BlockPos) {
        CENTER(BlockPos.ORIGIN),
        NORTH(BlockPos(0, 0, -1)),
        EAST(BlockPos(1, 0, 0)),
        SOUTH(BlockPos(0, 0, 1)),
        WEST(BlockPos(-1, 0, 0))
    }
}