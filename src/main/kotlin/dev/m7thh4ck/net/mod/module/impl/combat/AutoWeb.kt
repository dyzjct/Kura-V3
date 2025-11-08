package dev.m7thh4ck.net.mod.module.impl.combat

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.GameLoopEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.combat.HolePush.doHolePush
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine
import dev.m7thh4ck.net.util.entity.EntityUtil.canMove
import dev.m7thh4ck.net.util.entity.getTargetSpeed
import dev.m7thh4ck.net.util.helper.ChatUtil
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.math.vector.VectorUtils.toBlockPos
import dev.m7thh4ck.net.util.other.SurroundUtils
import dev.m7thh4ck.net.util.other.SurroundUtils.checkHole
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbar
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbarBypass
import dev.m7thh4ck.net.util.player.PlayerUtil.spoofSneaking
import dev.m7thh4ck.net.util.player.getTarget
import dev.m7thh4ck.net.util.world.BlockUtil.fastPos
import dev.m7thh4ck.net.util.world.BlockUtil.getNeighbor
import net.minecraft.block.CobwebBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object AutoWeb : Module(
    name = "AutoWeb",
    category = Category.Combat,
) {
    private var spoofRotations by setting("Rotate", false)
    private var holeCheck by setting("HoleCheck", true)
    private var betterPush by setting("BetterPush", false)
    private var eatingPause by setting("EatingPause", true)
    private var facePlace by setting("FacePlace", false)
    private var multiPlace by setting("MultiPlace", true)
    private var spoofBypass by setting("SpoofBypass", false)
    private var inside by setting("Inside", false)
    private var strictDirection by setting("StrictDirection", false)
    private var air by setting("AirPlace", false)
    private var range by setting("Range", 5.0, 1.0..6.0, 1.0)
    private var predictTicks by setting("PredictTicks", 8, 0..20, 1)
    private var smartDelay by setting("SmartDelay", false)
    private var delay by setting("minDelay", 25, 0..500, 1)
    private var maxDelay by setting("MaxDelay", 400, 0..1000, 1) { smartDelay }
    private var debug by setting("Debug", false)
    var target: PlayerEntity? = null
    private var timerDelay = TimerUtils()

    override fun getHudInfo(): String {
        target?.let {
            return "${it.name.string} ${getTargetSpeed(it) > 20.0}"
        } ?: return "Waiting..."
    }

    init {
        onEnable {
            timerDelay.reset()
        }
        eventListener<GameLoopEvent.Tick> {
            val webSlot = InventoryUtil.findItemInHotbar(Items.COBWEB)
            if ((eatingPause && player.isUsingItem) || webSlot == null) {
                return@eventListener
            }
            target = getTarget(range)
            target?.let {
                val targetDistance = getPredictedBlockPos(it, predictTicks)
                if (doHolePush(
                        it.blockPos.up(),
                        true,
                        null,
                        null
                    ) != null && betterPush && HolePush.isEnabled()
                ) return@eventListener
                fun place(delay: Long) {
                    onPacket@ fun packet(pos: BlockPos, checkDown: Boolean = false) {
                        if (checkDown && world.getBlockState(pos.down()).block is CobwebBlock) return
                        PacketMine.blockData?.let { data ->
                            if (data.blockPos == pos) return@onPacket
                        }

                        if (world.isAir(pos) && (getNeighbor(pos, strictDirection) != null || air)) {
                            if (timerDelay.tickAndReset(delay)) {
                                if (spoofRotations) {
                                    RotationManager.addRotation(pos,5)
                                }
                                player.spoofSneaking {
                                    if (!spoofBypass) {
                                        spoofHotbar(webSlot) {
                                            connection.sendPacket(
                                                fastPos(pos, true)
                                            )
                                        }
                                    } else {
                                        spoofHotbarBypass(webSlot) {
                                            connection.sendPacket(
                                                fastPos(pos, true)
                                            )
                                        }
                                    }
                                }
                                if (debug) ChatUtil.sendRawMessage("Placing")
                            }
                        }
                    }

                    getNeighbor(targetDistance.up(), strictDirection)?.let {
                        if (facePlace) {
                            packet(targetDistance.up(), true)
                        }
                    }

                    getNeighbor(targetDistance, strictDirection)?.let {
                        if (inside) {
                            packet(targetDistance)
                        }
                    }

                    getNeighbor(targetDistance.down(), strictDirection)?.let {
                        packet(targetDistance.down())
                    }

                    if (multiPlace) {
                        packet(it.pos.add(0.3, 0.3, 0.3).toBlockPos())
                        packet(it.pos.add(-0.3, 0.3, -0.3).toBlockPos())
                        packet(it.pos.add(-0.3, 0.3, 0.3).toBlockPos())
                        packet(it.pos.add(0.3, 0.3, -0.3).toBlockPos())
                        if (facePlace) {
                            packet(it.pos.add(0.3, 0.3, 0.3).toBlockPos().up(), true)
                            packet(it.pos.add(-0.3, 0.3, -0.3).toBlockPos().up(), true)
                            packet(it.pos.add(-0.3, 0.3, 0.3).toBlockPos().up(), true)
                            packet(it.pos.add(0.3, 0.3, -0.3).toBlockPos().up(), true)
                        }
                    }
                }

                if (checkHole(it) != SurroundUtils.HoleType.NONE && it.onGround && holeCheck) return@eventListener

                val useDelay = if (smartDelay) {
                    if (getTargetSpeed(it) < 20.0) maxDelay else delay
                } else {
                    delay
                }

                place(useDelay.toLong())
            }
        }
    }

    private fun getPredictedBlockPos(target: PlayerEntity, ticks: Int): BlockPos {
        val motionX = (target.x - target.lastRenderX).coerceIn(-0.6, 0.6)
        val motionY = (target.y - target.lastRenderY).coerceIn(-0.5, 0.5)
        val motionZ = (target.z - target.lastRenderZ).coerceIn(-0.6, 0.6)

        val entityBox = target.boundingBox
        var targetBox = entityBox

        for (tick in 0..ticks) {
            targetBox =
                canMove(targetBox, motionX, motionY, motionZ) ?: canMove(targetBox, motionX, 0.0, motionZ)
                        ?: canMove(
                    targetBox, 0.0, motionY, 0.0
                ) ?: break
        }

        val offsetX = targetBox.minX - entityBox.minX
        val offsetY = targetBox.minY - entityBox.minY
        val offsetZ = targetBox.minZ - entityBox.minZ
        val motion = Vec3d(offsetX, offsetY, offsetZ).toBlockPos()
        val pos = target.blockPos

        return pos.add(motion)
    }
}