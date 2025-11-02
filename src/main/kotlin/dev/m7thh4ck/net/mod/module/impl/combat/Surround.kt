package dev.m7thh4ck.net.mod.module.impl.combat

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.GameLoopEvent
import dev.m7thh4ck.net.event.impl.PlayerMoveEvent
import dev.m7thh4ck.net.event.impl.TickEvent
import dev.m7thh4ck.net.event.impl.WorldEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.movement.Step
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine
import dev.m7thh4ck.net.mod.module.impl.render.PlaceRender
import dev.m7thh4ck.net.util.entity.EntityUtil
import dev.m7thh4ck.net.util.entity.EntityUtil.getGroundPos
import dev.m7thh4ck.net.util.entity.EntityUtil.preventEntitySpawning
import dev.m7thh4ck.net.util.extension.sq
import dev.m7thh4ck.net.util.helper.ChatUtil
import dev.m7thh4ck.net.util.math.TickTimer
import dev.m7thh4ck.net.util.math.TimeUnit
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.math.vector.distanceSqTo
import dev.m7thh4ck.net.util.other.EnumMap
import dev.m7thh4ck.net.util.other.SurroundUtils
import dev.m7thh4ck.net.util.other.SurroundUtils.betterPosition
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbar
import dev.m7thh4ck.net.util.player.InventoryUtil.spoofHotbarBypass
import dev.m7thh4ck.net.util.player.PlayerUtil
import dev.m7thh4ck.net.util.player.PlayerUtil.isMoving
import dev.m7thh4ck.net.util.player.PlayerUtil.realSpeed
import dev.m7thh4ck.net.util.player.PlayerUtil.sendSequencedPacket
import dev.m7thh4ck.net.util.player.PlayerUtil.spoofSneaking
import dev.m7thh4ck.net.util.world.BlockUtil.getHitVec
import dev.m7thh4ck.net.util.world.BlockUtil.getHitVecOffset
import dev.m7thh4ck.net.util.world.BlockUtil.getNeighborSequence
import dev.m7thh4ck.net.util.world.BlockUtil.getVisibleSides
import dev.m7thh4ck.net.util.world.PlaceInfo
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSets
import net.minecraft.block.Blocks
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

object Surround : Module(
    name = "Surround",
    category = Category.Combat,
) {
    private var placeDelay = setting("PlaceDelay", 50, 0..1000, 1)
    private var multiPlace = setting("MultiPlace", 2, 1..4, 1)
    private var groundCheck = setting("GroundCheck", true)
    private var strictDirection = setting("StrictDirection", false)
    private var autoCenter = setting("AutoCenter", true)
    private var rotation = setting("Rotation", false)
    private var attackCrystal = setting("AttackCrystal", false)
    private val spoofBypass = setting("SpoofBypass", false)
    private val placing = EnumMap<SurroundOffset, List<PlaceInfo>>()
    private val placingSet = LongOpenHashSet()
    private val pendingPlacing = Long2LongMaps.synchronize(Long2LongOpenHashMap()).apply { defaultReturnValue(-1L) }
    private val placed = LongSets.synchronize(LongOpenHashSet())
    private val toggleTimer = TickTimer(TimeUnit.TICKS)
    private var placeTimer = TickTimer()
    private var safeTimer = TimerUtils()
    private var holePos: BlockPos? = null
    private var enableTicks = 0


    init {
        eventListener<WorldEvent.ServerBlockUpdate> { event ->
            val pos = event.pos
            if (!event.newState.isReplaceable) {
                val long = pos.asLong()
                if (placingSet.contains(long)) {
                    pendingPlacing.remove(long)
                    placed.add(long)
                }
            } else {
                val relative = pos.subtract(player.betterPosition)
                if (SurroundOffset.entries.any { it.offset == relative } && checkColliding(pos)) {
                    if (safeTimer.tickAndReset(50L)) {
                        for (entity in world.entities) {
                            if (entity !is EndCrystalEntity) continue
                            if (!entity.boundingBox.intersects(Box(pos))) continue
                            if (entity.distanceSqTo(player.pos) > 5.sq) continue
                            if (!entity.isAlive) continue
                            if (!attackCrystal.value) continue
                            connection.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, false))
                            connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
                        }
                    }
                    getNeighbor(pos)?.let { placeInfo ->
                        placingSet.add(placeInfo.placedPos.asLong())
                        placeBlock(placeInfo)
                    }
                }
            }
        }
        eventListener<TickEvent.Pre> {
            if (Step.isEnabled() && EntityUtil.isMoving()) {
                placing.clear()
                placingSet.clear()
                pendingPlacing.clear()
                placed.clear()
                holePos = null
            }
            enableTicks++
        }

        eventListener<GameLoopEvent.Tick>(0) {
            if (groundCheck.value) {
                if (!player.onGround) {
                    if (this.isEnabled()) disable()
                    return@eventListener
                }
            }

            var playerPos = player.betterPosition
            val isInHole =
                player.onGround && (player.realSpeed < 0.1) && (SurroundUtils.checkHole(playerPos) == SurroundUtils.HoleType.OBBY)

            if (!world.getBlockState(playerPos.down())
                    .getCollisionShape(world, playerPos).isEmpty && world.getBlockState(playerPos.down())
                    .getCollisionShape(world, playerPos).boundingBox == null
            ) {
                playerPos = world.getGroundPos(player).up()
            }

            if (isInHole || holePos == null) {
                holePos = playerPos
            }

            updatePlacingMap(playerPos)

            if (placing.isNotEmpty() && placeTimer.tickAndReset(placeDelay.value)) {
                runPlacing()
            }
        }

        eventListener<PlayerMoveEvent> {
            if (Step.isEnabled() && isMoving()) disable()
        }

        onEnable {
            if (autoCenter.value) {
                PlayerUtil.autoCenter()
            }
        }

        onDisable {
            placeTimer.reset(-114514L)
            toggleTimer.reset()

            placing.clear()
            placingSet.clear()
            pendingPlacing.clear()
            placed.clear()

            holePos = null
            enableTicks = 0
        }
    }

    private fun updatePlacingMap(playerPos: BlockPos) {
        pendingPlacing.forEach {
            if (if (!world.getBlockState(BlockPos.fromLong(it.value)).isReplaceable) {
                    placed.add(it.value)
                    true
                } else {
                    false
                }
            ) {
                pendingPlacing.remove(it.key)
            }
        }

        if (placing.isEmpty() && (pendingPlacing.isEmpty() || pendingPlacing.filter { System.currentTimeMillis() > it.key }
                .isEmpty())) {
            placing.clear()
            placed.clear()
        }

        val tempPosition: MutableMap<BlockPos, SurroundOffset> = HashMap()

        for (surroundOffset in SurroundOffset.entries) {
            val offsetPos = playerPos.add(surroundOffset.offset)
            if (!world.getBlockState(offsetPos).isReplaceable) continue

            if (isEntityIntersecting(offsetPos)) {
                for (offset in SurroundOffset.entries) {
                    val extendedOffset = offsetPos.add(offset.offset)

                    if (extendedOffset == playerPos) {
                        continue
                    }
                    if (!isEntityIntersecting(extendedOffset)) {
                        tempPosition[extendedOffset] = offset
                    } else {
                        for (offsetTry in SurroundOffset.entries) {
                            val tryPos = extendedOffset.add(offsetTry.offset)
                            if (tryPos == playerPos) {
                                continue
                            }
                            if (isEntityIntersecting(tryPos)) continue
                            if (!world.isAir(tryPos)) continue
                            tempPosition[tryPos] = surroundOffset
                        }
                    }
                }
            } else {
                tempPosition[offsetPos] = surroundOffset
            }

        }

        tempPosition.forEach {
            getNeighborSequence(it.key, 2, 5.0f, strictDirection.value, false)?.let { list ->
                placing[it.value] = list
                list.forEach { placeInfo ->
                    placingSet.add(placeInfo.placedPos.asLong())
                }
            }
        }

    }

    private fun isEntityIntersecting(pos: BlockPos): Boolean {
        for (target in world.entities.filterIsInstance<PlayerEntity>()) {
            if (target.boundingBox.intersects(Box(pos))) {
                return true
            }
        }

        return false
    }

    private fun runPlacing() {
        var placeCount = 0
        val iterator = placing.values.iterator()
        while (iterator.hasNext()) {
            val list = iterator.next()
            var allPlaced = true
            loop@ for (placeInfo in list) {
                val long = placeInfo.placedPos.asLong()
                if (PacketMine.blockData?.blockPos == placeInfo.placedPos) continue
                if (placed.contains(long)) continue
                allPlaced = false

                if (System.currentTimeMillis() <= pendingPlacing[long]) continue

                if (safeTimer.tickAndReset(50)) {
                    for (entity in world.entities) {
                        if (entity !is EndCrystalEntity) continue
                        if (!entity.isAlive) continue
                        if (!entity.boundingBox.intersects(Box(placeInfo.placedPos))) continue
                        if (!attackCrystal.value) continue
                        connection.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, false))
                        connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
                        safeTimer.reset()
                    }
                }
                placeBlock(placeInfo)
                placeCount++
                if (placeCount >= multiPlace.value) return
            }

            if (allPlaced) iterator.remove()
        }

    }

    private fun getNeighbor(pos: BlockPos): PlaceInfo? {
        for (side in Direction.entries) {
            val offsetPos = pos.offset(side)
            val oppositeSide = side.opposite

            if (strictDirection.value && !getVisibleSides(offsetPos, true).contains(oppositeSide)) continue
            if (world.getBlockState(offsetPos).isReplaceable) continue

            val hitVec = getHitVec(offsetPos, oppositeSide)
            val hitVecOffset = getHitVecOffset(oppositeSide)

            return PlaceInfo(offsetPos, oppositeSide, 0.0, hitVecOffset, hitVec, pos)
        }

        return null
    }

    private fun checkColliding(pos: BlockPos): Boolean {
        val box = Box(pos)

        return world.entities.none {
            it.isAlive && it.preventEntitySpawning && it.boundingBox.intersects(box)
        }
    }

    private fun placeBlock(placeInfo: PlaceInfo) {
        val slot = getSlot() ?: run {
            disable()
            return
        }
        if (!world.isAir(placeInfo.placedPos)) return
        player.spoofSneaking {
            if (rotation.value) {
                var eyeHeight = player.getEyeHeight(player.pose)
                if (!player.isSneaking) eyeHeight -= 0.08f
                RotationManager.addRotation(
                    placeInfo.hitVec, 2
                )
            }
            if (!spoofBypass.value) {
                spoofHotbarBypass(slot) {
                    sendSequencedPacket(world) {
                        placeInfo.toPlacePacket(Hand.MAIN_HAND, sequence = it)
                    }
                }
            } else {
                spoofHotbar(slot) {
                    sendSequencedPacket(world) {
                        placeInfo.toPlacePacket(Hand.MAIN_HAND, sequence = it)
                    }
                }
            }
            connection.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
            PlaceRender.renderBlocks[placeInfo.placedPos] = System.currentTimeMillis()
        }
        val blockState = Blocks.OBSIDIAN.getPlacementState(
            ItemPlacementContext(
                world,
                player,
                Hand.MAIN_HAND,
                player.getStackInHand(Hand.MAIN_HAND),
                BlockHitResult(placeInfo.hitVec, placeInfo.side, placeInfo.pos, false)
            )
        )
        blockState?.let {
            val soundType = blockState.block.getSoundGroup(blockState)
            world.playSound(
                player,
                placeInfo.pos,
                soundType.placeSound,
                SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0f) / 2.0f,
                soundType.getPitch() * 0.8f
            )
        }

        pendingPlacing[placeInfo.placedPos.asLong()] = System.currentTimeMillis() + 50L
    }

    private fun getSlot(): Int? {
        val slot = InventoryUtil.findItemInHotbar(Items.OBSIDIAN)

        return if (slot == null) {
            ChatUtil.sendRawMessage("No obsidian in hotbar!")
            null
        } else {
            slot
        }
    }

    private enum class SurroundOffset(val offset: BlockPos) {
        DOWN(BlockPos(0, -1, 0)), NORTH(BlockPos(0, 0, -1)), EAST(BlockPos(1, 0, 0)), SOUTH(BlockPos(0, 0, 1)), WEST(
            BlockPos(-1, 0, 0)
        )
    }

    private fun PlaceInfo.toPlacePacket(hand: Hand, sequence: Int = 0) =
        PlayerInteractBlockC2SPacket(
            hand,
            BlockHitResult(hitVec, side, pos, false),
            sequence
        )
}