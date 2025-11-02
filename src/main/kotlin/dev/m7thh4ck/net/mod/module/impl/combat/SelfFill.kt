package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.extension.sq
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.math.vector.distanceSqTo
import dev.m7thh4ck.net.util.player.InteractionUtil
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.PlayerUtil.sendSequencedPacket
import dev.m7thh4ck.net.util.world.BlockUtil
import dev.m7thh4ck.net.util.world.BlockUtil.DownPlace
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.*
import kotlin.math.floor
import kotlin.math.roundToInt

object SelfFill : Module("SelfFill+", Category.Combat) {
    private var attackCrystal = setting("AttackCrystal", false)
    private val InstantMod by setting("InstantMod", Instant.Scanner)
    private val FakeJumpMod by setting("fakeJumpMod", fakeJump.Scanner)
    private var xzOffset by setting("fakeJumpXzOffset", 0.0, -1.0..1.0, 0.1) { FakeJumpMod == fakeJump.ScannerBeta }
    private val lagJump by setting(
        "LagFakeJump",
        true
    ) { FakeJumpMod == fakeJump.Scanner || FakeJumpMod == fakeJump.dyzjct || FakeJumpMod == fakeJump.ScannerBeta }
    val rotate by setting("Rotate", true)

    //CC Instant
    private val QAQ by setting("1337", true) { InstantMod == Instant.CC }
    private var AWA by setting("1337", -1300, -1337..1337, 1) { QAQ || InstantMod == Instant.CC }

    //AAC Instant
    private var AACFor by setting("AACFor", 3.0, 0.0..3.0, 1.0) { InstantMod == Instant.AAC }
    private var AACOffset by setting("AACOffset", 5.0, -5.0..6.0, 1.0) { InstantMod == Instant.AAC }

    //Org Instant
    private val Lag by setting("Lag", true) { InstantMod == Instant.Org }

    //Scanner Instant
    private val OneTick by setting("OneTick", true) { InstantMod == Instant.Scanner }
    private var Offset by setting("Offset", 5.0, -10.0..10.0, 0.1) { InstantMod == Instant.Scanner }
    private val Y by setting("0.2", true) { InstantMod == Instant.Scanner }
    private val Tick by setting("Tick", true) { InstantMod == Instant.Scanner }
    private val OneTicks by setting("XZTick", true) { InstantMod == Instant.Scanner }
    private val bypass by setting("Bypass", true)
    private val AntiCheatPacket by setting("AntiCheat.cc", false)
    private val AntiCheatOffset by setting("AntiCheatOffset", false)


    private val BurrowDelay by setting("Delay", 10, 0..10000, 1)
    private val InvTimer = TimerUtils()
    private var safeTimer = TimerUtils()
    private var status = 0
    private var moved = false
    var pos: BlockPos? = null

    private var ignore = false

    init {

        eventListener<PacketEvent.Receive> { p ->

            if (p.packet is PositionAndOnGround) {
                val pack: PositionAndOnGround = p.packet
                val floored: BlockPos = getPlayerPosFloored(player, 0.2)
                if (world.getBlockState(floored).isSolid
                    && !world.getBlockState(floored.add(0, 1, 0)).isSolid
                ) {
                    if (bypass && !ignore) {
                        player.prevY = 0.0
                        if (world.getBlockState(
                                getPlayerPosFloored(player).add(0, 1, 0)
                            ).isOpaque
                        ) {
                            player.setPosition(player.x, player.y - 1e-10, player.z)
                            ignore = true
                            player.networkHandler.sendPacket(
                                PositionAndOnGround(
                                    player.x,
                                    player.y,
                                    player.z,
                                    false
                                )
                            )
                            player.networkHandler.sendPacket(
                                PositionAndOnGround(
                                    player.x,
                                    player.y + 1000,
                                    player.z,
                                    false
                                )
                            )
                            ignore = false
                            pack.y = Math.round(pack.y) - 1e-10
                        }
                    }
                }
            }
        }
    }

    init {
        eventListener<TickEvent.Post> {
            if (fullNullCheck()) return@eventListener
            if (!player.isOnGround) return@eventListener
            if (moved && pos != null && !world.isAir(pos)) {
                return@eventListener
            }
            val obsSlot = InventoryUtil.findItemInHotbar(Items.OBSIDIAN)
            val eChestSlot = InventoryUtil.findItemInHotbar(Items.ENDER_CHEST)

            if (obsSlot == null && eChestSlot == null) {
                disable()
                return@eventListener
            }

            if (safeTimer.tickAndReset(50L)) {
                for (entity in world.entities) {
                    if (entity !is EndCrystalEntity) continue
                    if (entity.distanceSqTo(player.pos) > 3.5.sq) continue
                    if (!entity.isAlive) continue
                    if (!attackCrystal.value) continue
                    if (rotate) {
                        RotationManager.addRotation(entity.pos, 0)
                    }
                    connection.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, false))
                    connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
                }
                safeTimer.reset()
            }
            DownPlace()

            if (safeTimer.tickAndReset(50L)) {
                for (entity in world.entities) {
                    if (entity !is EndCrystalEntity) continue
                    if (entity.distanceSqTo(player.pos) > 3.5.sq) continue
                    if (!entity.isAlive) continue
                    if (!attackCrystal.value) continue
                    connection.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, false))
                    connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
                }
                safeTimer.reset()
            }

            fakeJump()
            if (lagJump) {
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 1.166109260938215,
                        player.z,
                        false
                    )
                )
            }
            val pos1 = BlockPos(
                MathHelper.floor(player.x + 0.3), MathHelper.floor(player.y + 0.5), MathHelper.floor(
                    player.z + 0.3
                )
            )
            val pos2 = BlockPos(
                MathHelper.floor(player.x - 0.3), MathHelper.floor(player.y + 0.5), MathHelper.floor(
                    player.z + 0.3
                )
            )
            val pos3 = BlockPos(
                MathHelper.floor(player.x + 0.3), MathHelper.floor(player.y + 0.5), MathHelper.floor(
                    player.z - 0.3
                )
            )
            val pos4 = BlockPos(
                MathHelper.floor(player.x - 0.3), MathHelper.floor(player.y + 0.5), MathHelper.floor(
                    player.z - 0.3
                )
            )
            connection.sendPacket(PlayerMoveC2SPacket.LookAndOnGround(player.yaw, 90f, player.onGround))
            if (rotate) {
                RotationManager.addRotation(player.yaw, 90f, 0)
            }
            InventoryUtil.spoofHotbar(obsSlot ?: eChestSlot ?: -1) {
                if (canPlace(player.blockPos)) {
                    sendSequencedPacket(world) {
                        PlayerInteractBlockC2SPacket(
                            Hand.MAIN_HAND, BlockHitResult(
                                player.blockPos.toCenterPos(), Direction.UP, player.blockPos, true
                            ), it
                        )
                    }
                }
                if (canPlace(pos1)) {
                    InteractionUtil.placeBlock(
                        pos1, InteractionUtil.Interact.Legit,
                        InteractionUtil.PlaceMode.Packet, true
                    )
                }
                if (canPlace(pos2)) {
                    InteractionUtil.placeBlock(
                        pos2, InteractionUtil.Interact.Legit,
                        InteractionUtil.PlaceMode.Packet, true
                    )
                }
                if (canPlace(pos3)) {
                    InteractionUtil.placeBlock(
                        pos3, InteractionUtil.Interact.Legit,
                        InteractionUtil.PlaceMode.Packet, true
                    )
                }
                if (canPlace(pos4)) {
                    InteractionUtil.placeBlock(
                        pos4, InteractionUtil.Interact.Legit,
                        InteractionUtil.PlaceMode.Packet, true
                    )
                }
            }
            instant()
            antiCheat()
            swing()
            if (InvTimer.tickAndReset(BurrowDelay)) {
                disable()
                InvTimer.reset()
            }
        }
    }

    private fun antiCheat() {

        if (AntiCheatPacket) {
            for (i in 0..15)
                player.networkHandler.sendPacket(PositionAndOnGround(player.x, player.y - 30, player.z, false))
        }
        if (AntiCheatOffset) {
            val pos: BlockPos = getFlooredPosition(player as Entity).add(0, 5, 0)
            player.networkHandler.sendPacket(
                PositionAndOnGround(
                    pos.x + 0.0,
                    pos.y.toDouble(),
                    pos.z + 0.0,
                    true
                )
            )
            sendPlayerPos(player.x, pos.y.toDouble() + 0.2, player.z, true)
        }

    }

    private fun fakeJump() {
        when (FakeJumpMod) {
            fakeJump.Scanner -> {
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 0.4199999868869781,
                        player.z,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 0.7531999805212017,
                        player.z,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 0.9999957640154541,
                        player.z,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 1.1661092609382138,
                        player.z,
                        false
                    )
                )
            }

            fakeJump.ScannerBeta -> {
                if (!fakeBBoxCheck(
                        player,
                        Vec3d(0.0, 0.0, 0.0),
                        true
                    )
                ) {
                    val pos: BlockPos? =
                        getOffsetBlock(player as PlayerEntity)
                    if (!world.getBlockState(
                            getFlooredPosition(
                                player as Entity
                            ).offset(Direction.UP, 2)
                        ).block.equals(Blocks.AIR)
                    ) {
                        for (facing in Direction.entries) {
                            if (facing !== Direction.UP) {
                                if (facing !== Direction.DOWN) {
                                    val offPos: BlockPos =
                                        getFlooredPosition(player as Entity)
                                            .offset(facing)
                                    if (world.isAir(offPos) && world.isAir(offPos.offset(Direction.UP))) {
                                        player.networkHandler.sendPacket(
                                            PositionAndOnGround(
                                                player.x + (floor(player.x) + 0.5 + xzOffset - player.x) / 2.0,
                                                player.y + 0.2,
                                                player.z + (floor(player.z) + 0.5 + xzOffset - player.z) / 2.0,
                                                false
                                            )
                                        )
                                        player.networkHandler.sendPacket(
                                            PositionAndOnGround(
                                                player.x + (floor(player.x) + 0.5 + xzOffset - player.x) / 2.0,
                                                player.y + 0.2,
                                                player.z + (floor(player.z) + 0.5 + xzOffset - player.z) / 2.0,
                                                false
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        for (facing in Direction.entries) {
                            if (facing !== Direction.UP) {
                                if (facing !== Direction.DOWN) {
                                    val offPos: BlockPos =
                                        getFlooredPosition(player as Entity)
                                            .offset(facing)
                                    if (world.isAir(offPos) && world.isAir(offPos.offset(Direction.UP))) {
                                        player.networkHandler.sendPacket(
                                            PositionAndOnGround(
                                                player.x + (floor(player.x) + 0.5 + xzOffset - player.x) / 2.0,
                                                player.y + 0.2,
                                                player.z + (floor(player.z) + 0.5 + xzOffset - player.z) / 2.0,
                                                false
                                            )
                                        )
                                        player.networkHandler.sendPacket(
                                            PositionAndOnGround(
                                                player.x + (floor(player.x) + 0.5 + xzOffset - player.x) / 2.0,
                                                player.y + 0.2,
                                                player.z + (floor(player.z) + 0.5 + xzOffset - player.z) / 2.0,
                                                false
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        if (pos == null || !world.getBlockState(
                                getFlooredPosition(player as Entity)
                                    .offset(Direction.UP, 2)
                            ).block.equals(Blocks.AIR)
                        ) {
                            return
                        }
                        val offX: Double =
                            pos.x + 0.5 - player.x
                        val offZ: Double =
                            pos.z + 0.5 - player.z
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                player.x + offX * 0.25,
                                player.y + 0.419999986886978,
                                player.z + offZ * 0.25,
                                false
                            )
                        )
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                player.x + offX * 0.5,
                                player.y + 0.7531999805212015,
                                player.z + offZ * 0.5,
                                false
                            )
                        )
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                player.x + offX * 0.75,
                                player.y + 1.001335979112147,
                                player.z + offZ * 0.75,
                                false
                            )
                        )
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                pos.x + 0.5,
                                player.y + 1.166109260938214,
                                pos.z + 0.5,
                                false
                            )
                        )
                    }
                } else {
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x,
                            player.y + 0.4199999868869781,
                            player.z,
                            false
                        )
                    )
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x,
                            player.y + 0.7531999805212017,
                            player.z,
                            false
                        )
                    )
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x,
                            player.y + 0.9999957640154541,
                            player.z,
                            false
                        )
                    )
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x,
                            player.y + 1.1661092609382138,
                            player.z,
                            false
                        )
                    )
                }
            }

            fakeJump.dyzjct -> {
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        floor(player.x) + 0.5,
                        player.y + 0.41999998688698,
                        floor(player.z) + 0.5,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        floor(player.x) + 0.5,
                        player.y + 0.7531999805212,
                        floor(player.z) + 0.5,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        floor(player.x) + 0.5,
                        player.y + 1.001335979,
                        floor(player.z) + 0.5,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        floor(player.x) + 0.5,
                        player.y + 1.16610926,
                        floor(player.z) + 0.5,
                        false
                    )
                )
            }
        }
    }


    private fun instant() {
        when (InstantMod) {
            Instant.CC -> {
                for (i in 0..19)
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x,
                            player.y + 1337,
                            player.z,
                            false
                        )
                    )
                if (QAQ) {
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x,
                            player.y - AWA,
                            player.z,
                            false
                        )
                    )
                }
            }

            Instant.AAC -> {
                var defaultOffset = true
                if (player.y >= 3.0) {
                    var i = -10
                    while (i < 10) {
                        if (i == -1) {
                            i = AACFor.toInt()
                        }
                        if (world.getBlockState(
                                getFlooredPosition(player as Entity).add(0, i, 0)
                            ).block.equals(Blocks.AIR) && world.getBlockState(
                                getFlooredPosition(player as Entity).add(0, i + 1, 0)
                            ).block.equals(Blocks.AIR)
                        ) {
                            val pos: BlockPos = getFlooredPosition(player as Entity).add(0, i, 0)
                            player.networkHandler.sendPacket(
                                PositionAndOnGround(
                                    pos.x + 0.3,
                                    pos.y.toDouble(),
                                    pos.z + 0.3,
                                    true
                                )
                            )
                            defaultOffset = false
                            break
                        }
                        ++i
                    }
                }
                if (defaultOffset) {
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            player.x + 0,
                            player.y + AACOffset,
                            player.z + 0,
                            true
                        )
                    )
                }
            }

            Instant.Org -> {
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 1.266109260938214,
                        player.z,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 3.000000458100,
                        player.z,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y - 2.26158745548,
                        player.z,
                        false
                    )
                )
                player.networkHandler.sendPacket(
                    PositionAndOnGround(
                        player.x,
                        player.y + 1.000000000000414,
                        player.z,
                        false
                    )
                )
                if (Lag) {
                    val pos: BlockPos = getFlooredPosition(player as Entity).add(0, (3).toInt(), 0)
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            pos.x + 0.0,
                            pos.y.toDouble() + 1.000000000000414,
                            pos.z + 0.0, false
                        )
                    )
                }
            }

            Instant.Scanner -> {
                if (OneTick) status = 2
                if (status == 2) {
                    val clip: Vec3i = getClip()
                    val pos: BlockPos = getFlooredPosition(player as Entity).add(0, (clip.y + Offset).toInt(), 0)
                    player.networkHandler.sendPacket(
                        PositionAndOnGround(
                            pos.x + 0.3,
                            pos.y.toDouble(),
                            pos.z + 0.3,
                            true
                        )
                    )
                    if (Y) {
                        sendPlayerPos(player.x, pos.y.toDouble() + 0.2, player.z, true)
                    }
                }
                if (Tick) {
                    if (OneTicks) {
                        val clip: Vec3i = getClip()
                        val pos: BlockPos = getFlooredPosition(player as Entity).add(0, (clip.y + Offset).toInt(), 0)
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                pos.x + 0.3,
                                pos.y.toDouble(),
                                pos.z + 0.3,
                                true
                            )
                        )
                        sendPlayerPos(player.x, player.y + 6, player.z, true)
                    }
                }
                status++
            }

        }


    }

    private fun canPlace(pos: BlockPos): Boolean {
        if (!BlockUtil.canBlockFacing(pos)) {
            return false
        }
        return if (!world.getBlockState(pos).isReplaceable) {
            false
        } else !hasEntity(pos)
    }

    private fun hasEntity(pos: BlockPos): Boolean {
        for (entity in world.getNonSpectatingEntities(Entity::class.java, Box(pos))) {
            if (entity == player) continue
            if (!entity.isAlive || entity is ItemEntity || entity is ExperienceOrbEntity
                || entity is ExperienceBottleEntity || entity is ArrowEntity || entity is EndCrystalEntity
            ) continue
            return true
        }
        return false
    }

    private fun getFlooredPosition(entity: Entity): BlockPos {
        return BlockPos(
            floor(entity.x).toInt(),
            entity.y.roundToInt().toDouble().toInt(),
            floor(entity.z).toInt()
        )
    }

    private fun sendPlayerPos(x: Double, y: Double, z: Double, onGround: Boolean) {
        player.networkHandler.sendPacket(PositionAndOnGround(x, y, z, onGround))
    }

    private fun getPlayerPosFloored(p_Player: Entity): BlockPos {
        return BlockPos(
            floor(p_Player.x).toInt(),
            floor(p_Player.y).toInt(),
            floor(p_Player.z).toInt()
        )
    }

    private fun getPlayerPosFloored(pos: Vec3d, h: Double): BlockPos {
        return BlockPos(floor(pos.x).toInt(), floor(pos.y + h).toInt(), floor(pos.z).toInt())
    }

    private fun getPlayerPosFloored(x: Double, y: Double, z: Double): BlockPos {
        return BlockPos(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())
    }

    private fun GetPlayerPosHighFloored(p_Player: Entity): BlockPos {
        return getPlayerPosFloored(p_Player, 0.2)
    }

    private fun getPlayerPosFloored(p_Player: Entity, y: Double): BlockPos {
        return BlockPos(
            floor(p_Player.x).toInt(),
            floor(p_Player.y + y).toInt(),
            floor(p_Player.z).toInt()
        )
    }

    private fun getClip(): Vec3i {
        val c: BlockPos = getPlayerPosFloored(player)

        if (isSelfBurrowClipPos(c.add(0, 5, 0))) return Vec3i(0, 5, 0)
        if (isSelfBurrowClipPos(c.add(0, 5, 0))) return Vec3i(0, 5, 0)
        if (isSelfBurrowClipPos(c.add(0, 5, 0))) return Vec3i(0, 5, 0)
        if (isSelfBurrowClipPos(c.add(0, 5, 0))) return Vec3i(0, 5, 0)
        return if (isSelfBurrowClipPos(c.add(0, 5, 0))) Vec3i(0, 5, 0) else Vec3i(0, 5, 0)
    }

    private fun isSelfBurrowClipPos(p: BlockPos): Boolean {
        return !world.getBlockState(p).isSolid && !world.getBlockState(
            p.add(
                0,
                4,
                0
            )
        ).isSolid
        return true
    }

    private fun fakeBBoxCheck(player: PlayerEntity, offset: Vec3d?, headcheck: Boolean): Boolean {
        val actualPos: Vec3d = player.velocity.add(offset)
        if (headcheck) {
            val playerPos: Vec3d = player.velocity
            return isAir(actualPos.add(0.3, 0.0, 0.3)) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(
                actualPos.add(
                    0.3,
                    0.0,
                    -0.3
                )
            ) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(
                actualPos.add(
                    0.3,
                    1.8,
                    0.3
                )
            ) && isAir(actualPos.add(-0.3, 1.8, 0.3)) && isAir(
                actualPos.add(
                    0.3,
                    1.8,
                    -0.3
                )
            ) && isAir(actualPos.add(-0.3, 1.8, 0.3)) && isAir(
                playerPos.add(
                    0.3,
                    2.8,
                    0.3
                )
            ) && isAir(playerPos.add(-0.3, 2.8, 0.3)) && isAir(playerPos.add(-0.3, 2.8, -0.3)) && isAir(
                playerPos.add(
                    0.3,
                    2.8,
                    -0.3
                )
            )
        }
        return isAir(actualPos.add(0.3, 0.0, 0.3)) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(
            actualPos.add(
                0.3,
                0.0,
                -0.3
            )
        ) && isAir(actualPos.add(-0.3, 0.0, 0.3)) && isAir(actualPos.add(0.3, 1.8, 0.3)) && isAir(
            actualPos.add(
                -0.3,
                1.8,
                0.3
            )
        ) && isAir(
            actualPos.add(0.3, 1.8, -0.3)
        ) && isAir(actualPos.add(-0.3, 1.8, 0.3))
    }

    private fun isAir(vec3d: Vec3d): Boolean {
        return world.getBlockState(
            vec3toBlockPos(
                vec3d,
                true
            )
        ).block.equals(Blocks.AIR)
    }

    private fun vec3toBlockPos(vec3d: Vec3d, Yfloor: Boolean): BlockPos {
        return if (Yfloor) {
            BlockPos(floor(vec3d.x).toInt(), floor(vec3d.y).toInt(), floor(vec3d.z).toInt())
        } else BlockPos(
            floor(vec3d.x).toInt(),
            vec3d.y.roundToInt().toDouble().toInt(),
            floor(vec3d.z).toInt()
        )
    }

    private fun vec3toBlockPos(vec3d: Vec3d): BlockPos {
        return BlockPos(
            Math.floor(vec3d.x).toInt(),
            Math.round(vec3d.y).toDouble().toInt(),
            Math.floor(vec3d.z).toInt()
        )
    }

    private fun canBur(vec3d: Vec3d): Boolean {
        val pos: BlockPos = vec3toBlockPos(vec3d)
        return world.isAir(pos) && world.isAir(pos.offset(Direction.UP)) && world.isAir(
            pos.offset(
                Direction.UP,
                2
            )
        )
    }

    private fun getOffsetBlock(player: PlayerEntity): BlockPos? {
        val vec3d1 = Vec3d(player.boundingBox.minX, player.boundingBox.minY, player.boundingBox.minZ)
        if (this.canBur(vec3d1)) {
            return vec3toBlockPos(vec3d1)
        }
        val vec3d2 = Vec3d(player.boundingBox.maxX, player.boundingBox.minY, player.boundingBox.minZ)
        if (this.canBur(vec3d2)) {
            return vec3toBlockPos(vec3d2)
        }
        val vec3d3 = Vec3d(player.boundingBox.minX, player.boundingBox.minY, player.boundingBox.maxZ)
        if (this.canBur(vec3d3)) {
            return vec3toBlockPos(vec3d3)
        }
        val vec3d4 = Vec3d(player.boundingBox.maxX, player.boundingBox.minY, player.boundingBox.maxZ)
        return if (this.canBur(vec3d4)) {
            vec3toBlockPos(vec3d4)
        } else null
    }

    private fun getVarOffsetList(x: Int, y: Int, z: Int): List<Vec3d> {
        val offsets = ArrayList<Vec3d>()
        offsets.add(Vec3d(x.toDouble(), y.toDouble(), z.toDouble()))
        return offsets
    }

    private fun getVarOffsets(x: Int, y: Int, z: Int): Array<Vec3d> {
        val offsets: List<Vec3d> = getVarOffsetList(x, y, z)
        val array = arrayOfNulls<Vec3d>(offsets.size)
        return offsets.toArray(array)
    }

    private fun checkSelf(pos: BlockPos?): Entity? {
        var test: Entity? = null
        val vec3dList: Array<Vec3d> = getVarOffsets(0, 0, 0)
        for (vec3d in vec3dList) {
            val position = BlockPos(pos).add(vec3d.x.toInt(), vec3d.y.toInt(), vec3d.z.toInt())
            for (entity in world.getNonSpectatingEntities(Entity::class.java, Box(position))) {
                if (entity !== player || test != null) continue
                test = entity
            }
        }
        return test
    }

    private enum class Instant(override val displayName: CharSequence) : DisplayEnum {
        CC("CC"),
        AAC("AAC"),
        Org("Org"),
        Scanner("Scanner"),
    }

    private enum class fakeJump(override val displayName: CharSequence) : DisplayEnum {
        dyzjct("dyzjct"),
        Scanner("Scanner"),
        ScannerBeta("ScannerBeta"),
    }
}

private fun <E> List<E>.toArray(array: Array<E?>): Array<E> {
    TODO("Not yet implemented")
}


