package dev.kura.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PlayerMotionEvent
import dev.kura.net.manager.impl.RotationManager
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.kura.net.mod.module.impl.player.PacketMine.hookPos
import dev.kura.net.utils.entity.EntityUtil.boxCheck
import dev.kura.net.utils.entity.EntityUtil.isInWeb
import dev.kura.net.utils.entity.EntityUtil.noCollision
import dev.kura.net.utils.extension.sq
import dev.kura.net.utils.helper.ChatUtil
import dev.kura.net.utils.math.TimerUtils
import dev.kura.net.utils.math.vector.distanceSqTo
import dev.kura.net.utils.other.SurroundUtils
import dev.kura.net.utils.other.SurroundUtils.checkHole
import dev.kura.net.utils.player.InventoryUtil
import dev.kura.net.utils.player.InventoryUtil.spoofHotbar
import dev.kura.net.utils.player.InventoryUtil.spoofHotbarBypass
import dev.kura.net.utils.player.PlayerUtil.spoofSneaking
import dev.kura.net.utils.player.getTarget
import dev.kura.net.utils.world.BlockUtil.fastPos
import dev.kura.net.utils.world.BlockUtil.getNeighbor
import net.minecraft.block.Blocks
import net.minecraft.block.PistonBlock
import net.minecraft.block.RedstoneBlock
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

object HolePush : Module(
    name = "HolePush",
    category = Category.Combat,
) {
    private val targetRange by setting("TargetRange", 7, 0..8, 1)
    private val range by setting("Range", 5, 0..6, 1)
    private val rotate by setting("Rotation", false)
    private val spoofBypass by setting("SpoofBypass", false)
    private val invSwap by setting("InvSwap", false)
    private val strictDirection = setting("StrictDirection", false)
    private val checkDown by setting("CheckDown", false)
    private val delay by setting("Delay", 50, 0..250, 1)
    private val airPlace by setting("AirPlace", false)
    private val autoToggle by setting("AutoToggle", true)
    private val pushDelay by setting("PushDelay", 250, 0..1000, 1) { !autoToggle }
    private val debug by setting("Debug", false)
    private val timer = TimerUtils()
    private val pushTimer = TimerUtils()
    private var stage = 0

    init {
        onEnable {
            stage = 0
        }
        onDisable {
            stage = 0
        }
        eventListener<PlayerMotionEvent> {
            var pistonSlot =
                InventoryUtil.findItemInHotbar(Items.PISTON) ?: InventoryUtil.findItemInHotbar(Items.STICKY_PISTON)
            var stoneSlot = InventoryUtil.findItemInHotbar(Items.REDSTONE_BLOCK)
            if (spoofBypass && invSwap) {
                pistonSlot =
                    InventoryUtil.findItemInInv(Items.PISTON) ?: InventoryUtil.findItemInInv(Items.STICKY_PISTON)
                stoneSlot = InventoryUtil.findItemInInv(Items.REDSTONE_BLOCK)
            }
            val target = getTarget(targetRange.toDouble())

            if (pistonSlot == null || stoneSlot == null || target == null) {
                if (autoToggle) {
                    toggle()
                }
                return@eventListener
            }
            if (autoToggle && stage >= 4) {
                toggle()
                return@eventListener
            }
            if (!autoToggle) {
                if ((world.isAir(target.blockPos) && checkHole(target) == SurroundUtils.HoleType.NONE) || player.usingItem)
                    return@eventListener
            }
            if (!world.isAir(target.blockPos.up(2))) return@eventListener
            val targetUp = target.blockPos.up()
            if (pushTimer.passedMs(pushDelay.toLong())) {
                if (!world.isAir(targetUp.up())) return@eventListener
                if (isInWeb(target)) return@eventListener
                if (debug) ChatUtil.sendRawMessage("Running.")
                doHolePush(targetUp, true, pistonSlot, stoneSlot)
                if (!world.isAir(target.blockPos)) doHolePush(targetUp, false, pistonSlot, stoneSlot)
            }
        }
    }

    fun doHolePush(
        targetPos: BlockPos,
        check: Boolean,
        pistonSlot: Int?,
        stoneSlot: Int?
    ): BlockPos? {
        fun checkPull(face: Direction): Boolean {
            return when (face) {
                Direction.NORTH -> {
                    !world.isAir(targetPos.offset(Direction.SOUTH)) || !world.isAir(
                        targetPos.offset(Direction.SOUTH).up()
                    )
                }

                Direction.SOUTH -> {
                    !world.isAir(targetPos.offset(Direction.NORTH)) || !world.isAir(
                        targetPos.offset(Direction.NORTH).up()
                    )
                }

                Direction.EAST -> {
                    !world.isAir(targetPos.offset(Direction.WEST)) || !world.isAir(
                        targetPos.offset(Direction.WEST).up()
                    )
                }

                Direction.WEST -> {
                    !world.isAir(targetPos.offset(Direction.EAST)) || !world.isAir(
                        targetPos.offset(Direction.EAST).up()
                    )
                }

                else -> false
            }
        }
        for (face in Direction.entries) {
            if (face == Direction.DOWN || face == Direction.UP) continue
            if (checkPull(face) && check) continue
            if (checkPull(face) && (!world.isAir(targetPos.offset(face).up()) && world.getBlockState(
                    targetPos.offset(
                        face
                    ).up()
                ).block !is RedstoneBlock)
            ) continue
            if (!world.entities.none {
                    it !is ItemEntity;it.isAlive;it.boundingBox.intersects(
                    Box(
                        targetPos.offset(
                            face
                        )
                    )
                )
                }) continue
            if (!world.noCollision(targetPos.offset(face)) && world.getBlockState(targetPos.offset(face)).block !is PistonBlock) continue
            if (!world.isAir(targetPos.offset(face)) && world.getBlockState(targetPos.offset(face)).block !is PistonBlock) continue
            if (debug) ChatUtil.sendRawMessage("getStonePos is not safe.")
            getRedStonePos(targetPos.offset(face), face)?.let {
                if (debug) ChatUtil.sendRawMessage("getStonePos is safe.")
                if (!world.isAir(it.pos.down()) || !checkDown) {
                    if (pistonSlot != null && stoneSlot != null) {
                        placeBlock(
                            targetPos.offset(face),
                            it.pos,
                            face,
                            pistonSlot,
                            stoneSlot,
                            !check
                        )
                        return it.pos
                    }
                } else if (!world.isAir(it.pos.down(2)) && checkDown && it.direction == Direction.DOWN) {
                    var obs = InventoryUtil.findItemInHotbar(Items.OBSIDIAN)
                    if (spoofBypass && invSwap) {
                        obs = InventoryUtil.findItemInInv(Items.OBSIDIAN) ?: obs
                    }
                    obs?.let { slot ->
                        if (timer.tickAndReset(delay)) {
                            if (rotate) RotationManager.addRotation(it.pos.down(), 3)
                            player.spoofSneaking {
                                if (spoofBypass) {
                                    spoofHotbarBypass(slot) {
                                        connection.sendPacket(
                                            fastPos(
                                                it.pos.down(),
                                                strictDirection.value,
                                                true
                                            )
                                        )
                                    }
                                } else {
                                    spoofHotbar(slot) {
                                        connection.sendPacket(
                                            fastPos(
                                                it.pos.down(),
                                                strictDirection.value,
                                                true
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: continue
        }
        return null
    }

    private fun placeBlock(
        blockPos: BlockPos,
        stonePos: BlockPos,
        face: Direction,
        pistonSlot: Int,
        stoneSlot: Int,
        mine: Boolean = false
    ) {
        if (!timer.passedMs(delay.toLong())) return
        fun spoofPlace(stone: Boolean, doToggle: Boolean = false) {
            if (debug) ChatUtil.sendRawMessage("Doing spoof place.")
            if (!stone) {
                RotationManager.stopRotation()
                face.let {
                    connection.sendPacket(
                        PlayerMoveC2SPacket.LookAndOnGround(
                            when (it) {
                                Direction.EAST -> -90f
                                Direction.NORTH -> 180f
                                Direction.SOUTH -> 0f
                                Direction.WEST -> 90f
                                else -> 0f
                            }, 0f, true
                        )
                    )
                }
            }
            if (!stone || world.isAir(stonePos)) {
                player.spoofSneaking {
                    if (spoofBypass) {
                        spoofHotbarBypass(if (!stone) pistonSlot else stoneSlot) {
                            connection.sendPacket(
                                fastPos(
                                    if (!stone) blockPos else stonePos,
                                    strictDirection.value,
                                    true
                                )
                            )
                        }
                    } else {
                        spoofHotbar(if (!stone) pistonSlot else stoneSlot) {
                            connection.sendPacket(
                                fastPos(
                                    if (!stone) blockPos else stonePos,
                                    strictDirection.value,
                                    true
                                )
                            )
                        }
                    }
                }
                swing()
            }
            RotationManager.startRotation()
            stage++
            if (!world.isAir(stonePos) && doToggle) {
                if (mine) hookPos(stonePos)
                if (autoToggle) {
                    toggle()
                } else {
                    pushTimer.reset()
                    stage = 0
                }
            }
            timer.reset()
            return
        }
        if (getNeighbor(blockPos, false) != null || airPlace) {
            if (world.isAir(blockPos)) {
                if (rotate) {
                    RotationManager.addRotation(blockPos, 3)
                }
                spoofPlace(stone = false, doToggle = true)
            } else {
                if (rotate && world.isAir(stonePos)) {
                    RotationManager.addRotation(stonePos, 3)
                }
                spoofPlace(stone = true, doToggle = true)
            }
        } else {
            if (rotate && world.isAir(stonePos)) {
                RotationManager.addRotation(stonePos, 3)
            }
            spoofPlace(stone = true, doToggle = false)
        }
    }

    private fun getRedStonePos(pos: BlockPos, direction: Direction): StonePos? {
        val face = when (direction) {
            Direction.EAST -> Direction.WEST
            Direction.WEST -> Direction.EAST
            Direction.NORTH -> Direction.SOUTH
            Direction.SOUTH -> Direction.NORTH
            else -> direction
        }
        for (facing: Direction in Direction.entries) {
            if (world.getBlockState(pos.offset(facing)).block == Blocks.REDSTONE_BLOCK) {
                return StonePos(pos.offset(facing), facing)
            }
            if (facing != face) {
                if (player.distanceSqTo(pos.offset(facing)) > range.sq) continue
                if (!boxCheck(Box(pos.offset(facing)))) continue
                if (!world.noCollision(pos.offset(facing))) continue
                if (!world.isAir(pos.offset(facing))) continue
                if (getNeighbor(pos.offset(facing), false) == null) continue
                return StonePos(pos.offset(facing), facing)
            }
        }
        return null
    }

    class StonePos(var pos: BlockPos, var direction: Direction)
}