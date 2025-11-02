package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.kura.net.manager.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.other.SurroundUtils.isHole
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.PlayerUtil.sendSequencedPacket
import dev.m7thh4ck.net.util.player.getTarget
import dev.m7thh4ck.net.util.world.BlockUtil.fastPos
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction


object AnchorAura : Module("AnchorAura", Category.Combat, true) {
    private var strictDirection by setting("StrictDirection", false)
    private var eatingPause by setting("EatingPause", true)
    private var rotate by setting("Rotate", true)
    private var range by setting("TargetRange", 5.0, 1.0..6.0, 1.0)
    private val UpDelay by setting("AnchorDelay", 50, 0..100, 1)
    private val PlaceDelay by setting("PlaceDelay", 2, 0..100, 1)
    private val glowDelay by setting("GlowDelay", 4, 0..100, 1)
    private val BlackDelay by setting("BlackDelay", 5, 0..100, 1)
    private val PlaceTimer = TimerUtils()
    private val glowTimer = TimerUtils()
    private val BlackTimer = TimerUtils()
    private val ObsTimer = TimerUtils()
    private val UpTimer = TimerUtils()
    var target: PlayerEntity? = null
    private var targetPos: BlockPos? = null
    init {
        eventListener<TickEvent.Post> {

            if (fullNullCheck()) return@eventListener
            if (!player.isOnGround) return@eventListener

            val Anchor = InventoryUtil.findItemInHotbar(Items.RESPAWN_ANCHOR)
            val Glow = InventoryUtil.findItemInHotbar(Items.GLOWSTONE)
            val obs = InventoryUtil.findItemInHotbar(Items.OBSIDIAN)
            if ((eatingPause && player.isUsingItem) ||Anchor == null && Glow == null && obs == null) {
                return@eventListener
            }
            fun doRotate(bp: BlockPos) {
                if (rotate) {
                    RotationManager.addRotation(
                        bp,
                        0
                    )

                }
            }
            target = getTarget(range)
            target?.let {
                if (isHole(target!!.blockPos)
                    || world.getBlockState(target!!.blockPos.up(2)).block.equals(Blocks.RESPAWN_ANCHOR)
                ) {
                    targetPos = target!!.blockPos.up(2)
                    if (UpTimer.tickAndReset(UpDelay)) {
                        targetPos?.let { it1 -> doRotate(it1) }



                        doPlace(targetPos)
                        UpTimer.reset()
                    }

                    if (target == null){
                        return@eventListener
                    }
                }
            }
        }
    }

    fun doPlace(pd: BlockPos?) {
        val Anchor = InventoryUtil.findItemInHotbar(Items.RESPAWN_ANCHOR)
        val Glow = InventoryUtil.findItemInHotbar(Items.GLOWSTONE)

        if (Anchor == null && Glow == null) {
            return
        }
        //Place
        if (PlaceTimer.tickAndReset(PlaceDelay)) {
            InventoryUtil.spoofHotbarBypass(Anchor ?: -1) {
                swing()
                connection.sendPacket(
                    pd?.let {
                        fastPos(
                            it,
                            strictDirection,
                            true
                        )
                    }
                )
            }
            PlaceTimer.reset()
        }
        //glow
        if (glowTimer.tickAndReset(glowDelay)) {
            InventoryUtil.spoofHotbar(Glow ?: -1) {
                swing()
                if (pd != null) {
                    sendSequencedPacket(world) {
                        PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND, BlockHitResult(
                                pd.toCenterPos(), Direction.UP, pd, true
                            ), it
                        )
                    }
                }


            }
            glowTimer.reset()
        }
     //Black
        if (BlackTimer.tickAndReset(BlackDelay)) {
            InventoryUtil.spoofHotbar(Anchor ?: -1) {
                swing()
                if (pd != null) {
                    sendSequencedPacket(world) {
                        PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND, BlockHitResult(
                                pd.toCenterPos(), Direction.UP, pd, true
                            ), it
                        )
                    }
                }
            }
            BlackTimer.reset()
        }
    }
}

