package dev.m7thh4ck.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.BlockEvent
import dev.kura.net.event.impl.GameLoopEvent
import dev.kura.net.event.impl.PlayerMotionEvent
import dev.kura.net.event.impl.Render3DEvent
import dev.kura.net.manager.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine.PacketType.Start
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine.PacketType.Stop
import dev.m7thh4ck.net.util.entity.EntityUtil.eyePosition
import dev.m7thh4ck.net.util.entity.scale
import dev.m7thh4ck.net.util.extension.sq
import dev.m7thh4ck.net.util.graphics.ESPRenderer
import dev.m7thh4ck.net.util.graphics.color.ColorRGB
import dev.m7thh4ck.net.util.graphics.easing.Easing
import dev.m7thh4ck.net.util.helper.ChatUtil
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.math.vector.distanceSqTo
import dev.m7thh4ck.net.util.player.InteractionUtil.findBestItem
import dev.m7thh4ck.net.util.player.InteractionUtil.findBestSlot
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.InventoryUtil.move
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbar
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbarBypass
import dev.m7thh4ck.net.util.player.PlayerUtil.sendSequencedPacket
import dev.m7thh4ck.net.util.world.BlockUtil.calcBreakTime
import dev.m7thh4ck.net.util.world.BlockUtil.canBreak
import dev.m7thh4ck.net.util.world.BlockUtil.getMiningSide
import net.minecraft.block.CobwebBlock
import net.minecraft.item.Items
import net.minecraft.item.SwordItem
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

@Suppress("unused")
object PacketMine : Module(
    name = "PacketMine",
    category = Category.Player,
) {
    private var mode by setting("Mode", PacketMode.Instant)
    private var safeSpamFactor by setting("SafeSpamFactor", 350, 1..1000, 1) { mode == PacketMode.Spam }
    private var breakTimeDelay by setting("BreakFactor", 1.3, 0.8..1.5, 0.001)
    //Relative 30 Tick 50 breakTime 1.4
     val relative by setting("RelativeDmg", 30, 20..30, 1)
     val Tick by setting("BreakTick", 50f, 40f..50f, 1f)
    private val inventoryDelay by setting("InvFixDelay", 10, 0..10, 1) { switchMode == SwitchMode.Bypass }
    private var switchMode by setting("SwitchMode", SwitchMode.Spoof)
    private var inventoryTool by setting("InvFix", false) { switchMode == SwitchMode.Bypass }
    private var rotate by setting("Rotate", true)
    private var Cev by setting("Crystal", false)
    private var eating by setting("EatingPause", true)
    private var swing by setting("Swing", false)
    private var Debug by setting("Debug", false)
    //255 32 32
    private val red by setting("Red", 255, 0..255, 1)
    private val green by setting("Green", 32, 0..255, 1)
    private val blue by setting("Blue", 32, 0..255, 1)
    private val renderer = ESPRenderer().apply { aFilled = 35; aOutline = 233 }
    private var packetTimer = TimerUtils()
    private var retryTimer = TimerUtils()
    private var hasPreStageFinished = false
    private var inventoryBypass = false
    private var packetSpamming = false
    private var fastSyncCheck = false
    private var forceRetry = false
    var blockData: BlockData? = null
    private val InvTimer = TimerUtils()
    override fun getHudInfo(): String {
        return mode.name
    }

    init {
        onDisable {
            blockData = null
            fastSyncCheck = false
            packetSpamming = false
            timerReset()
            renderer.clear()
        }

        onEnable {
            blockData = null
            fastSyncCheck = false
            packetSpamming = false
            timerReset()
            renderer.clear()
        }
        eventListener<BlockEvent> { event ->
            findBestSlot(event.pos, switchMode == SwitchMode.Bypass)?.let { item ->
                val crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL)
                if (crystal == null){
                    return@eventListener
                }
                BlockData(
                    event.pos,
                    event.facing,
                    item,
                    System.currentTimeMillis(),
                    calcBreakTime(event.pos, inventoryBypass)
                ).apply {
                    if (!canBreak(blockPos)) {
                        blockData = null
                        timerReset()
                        return@eventListener
                    }
                    if (world.getBlockState(blockPos).block is CobwebBlock && findBestItem(
                            blockPos,
                            inventoryBypass
                        ) !is SwordItem
                    ) {
                        blockData = null
                        timerReset()
                        return@eventListener
                    }
                    if (blockData?.blockPos == event.pos) {
                        retryTimer.reset()
                        return@eventListener
                    }
                    blockData = this
                    sendMinePacket(Start, this)
                    timerReset()
                    packetSpamming = true
                }
            }
        }

        eventListener<GameLoopEvent.Tick> {
            blockData?.let { blockData ->
                if (!world.isAir(blockData.blockPos)) {
                    packetSpamming = true
                    packetTimer.reset()
                    forceRetry = retryTimer.passed(blockData.breakTime * breakTimeDelay)
                } else {
                    forceRetry = false
                    retryTimer.reset()
                }
                if (packetTimer.passedMs(safeSpamFactor.toLong()) && world.isAir(blockData.blockPos)) {
                    if (Debug) ChatUtil.sendRawMessage("PacketSpamming-false")
                    packetSpamming = false
                }
                fastSyncCheck = if (!mode.ignoreCheck) {
                    world.isAir(blockData.blockPos)
                } else {
                    packetTimer.passed(blockData.breakTime)
                }
            }
        }

        eventListener<PlayerMotionEvent> {
            blockData?.let { blockData ->
                findBestSlot(blockData.blockPos, switchMode == SwitchMode.Bypass)?.let {
                    if (System.currentTimeMillis() - blockData.startTime >= blockData.breakTime && player.distanceSqTo(
                            blockData.blockPos
                        ) <= 5.15.sq
                    ) {
                        if (((mode.ignoreCheck && packetSpamming) || !fastSyncCheck) && (!player.isUsingItem || !eating)) {
                            if (rotate) RotationManager.addRotation(blockData.blockPos)
                            sendMinePacket(Stop, blockData)
                            hasPreStageFinished = true
                        }
                        if (mode.retry || forceRetry) {
                            if (!(player.isUsingItem && eating)) hookPos(blockData.blockPos)
                            hasPreStageFinished = false
                        }
                        if (mode.strict) {
                            PacketMine.blockData = BlockData(
                                blockData.blockPos,
                                blockData.facing,
                                it,
                                System.currentTimeMillis(),
                                calcBreakTime(blockData.blockPos, false)
                            )
                        }
                    }
                }

            }
        }

        eventListener<Render3DEvent> {
            blockData?.let { blockData ->
                renderer.add(
                    Box(blockData.blockPos).scale(
                        Easing.OUT_CUBIC.inc(
                            Easing.toDelta(
                                blockData.startTime,
                                blockData.breakTime
                            )
                        ).toDouble()
                    ), if (world.isAir(blockData.blockPos)) ColorRGB(32, 255, 32) else ColorRGB(red, green, blue)
                )
                renderer.render(it.matrices, true)
            }
        }
    }

    fun hookPos(blockPos: BlockPos) {
        blockData = null
        world.getBlockState(blockPos).onBlockBreakStart(world, blockPos, player)
        val side = getMiningSide(blockPos) ?: run {
            val vector = player.eyePosition.subtract(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)
            Direction.getFacing(vector.x.toFloat(), vector.y.toFloat(), vector.z.toFloat())
        }
        BlockEvent(blockPos, side).post()
        timerReset()
    }

    private fun timerReset() {
        packetTimer.reset()
        retryTimer.reset()
        hasPreStageFinished = false
    }

    private fun sendMinePacket(action: PacketType, blockData: BlockData) {
        blockData.mineTool?.let { hotbarSlot ->
            if (swing) swing()
            if (switchMode.spoof) {
                if (!switchMode.bypass) {
                    spoofHotbar(hotbarSlot) {
                        sendSequencedPacket(world) {
                            minePacket(action, blockData, it)
                        }
                    }
                } else {
                    if (inventoryTool) {
                        move(player.inventory.selectedSlot, hotbarSlot)
                    }

                    spoofHotbarBypass(hotbarSlot) {

                        sendSequencedPacket(world) {
                            minePacket(action, blockData, it)
                        }
                        if (InvTimer.tickAndReset(inventoryDelay)) {
                            if (inventoryTool) {
                                move(player.inventory.selectedSlot, hotbarSlot)
                            }
                            InvTimer.reset()
                        }
                    }
                }
            } else {
                if (switchMode != SwitchMode.Off) {
                    if (player.inventory.selectedSlot != hotbarSlot) {
                        player.inventory.selectedSlot = hotbarSlot
                    }
                    sendSequencedPacket(world) {
                        minePacket(action, blockData, it)
                    }
                }
            }
        }
    }

    private fun minePacket(actionType: PacketType, blockData: BlockData, sequence: Int): PlayerActionC2SPacket {
        if(Cev) {
        val crystal = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL)
            spoofHotbarBypass(crystal ?: -1) {
                sendSequencedPacket(world) {
                    PlayerInteractBlockC2SPacket(
                        Hand.MAIN_HAND, BlockHitResult(
                            blockData.blockPos.toCenterPos(), Direction.UP, blockData.blockPos, true
                        ), it
                    )
                }
            }
        }
        val side = getMiningSide(blockData.blockPos) ?: run {
            val vector = player.eyePosition.subtract(
                blockData.blockPos.x + 0.5,
                blockData.blockPos.y + 0.5,
                blockData.blockPos.z + 0.5
            )
            Direction.getFacing(vector.x.toFloat(), vector.y.toFloat(), vector.z.toFloat())

        }
        return PlayerActionC2SPacket(
            actionType.action,
            blockData.blockPos,
            side,
            sequence
        )
    }


    private enum class SwitchMode(val spoof: Boolean, val bypass: Boolean = false) {
        Spoof(true), Bypass(true, true), Swap(false), Off(false)
    }

    enum class PacketMode(val strict: Boolean, val retry: Boolean = false, val ignoreCheck: Boolean = false) {
        Instant(false), Spam(false, ignoreCheck = true), Packet(true), Legit(true, true)
    }

    enum class PacketType(val action: PlayerActionC2SPacket.Action) {
        Start(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK),
        Abort(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK),
        Stop(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK)
    }

    class BlockData(
        val blockPos: BlockPos,
        val facing: Direction,
        val mineTool: Int?,
        val startTime: Long,
        val breakTime: Float
    )
}
