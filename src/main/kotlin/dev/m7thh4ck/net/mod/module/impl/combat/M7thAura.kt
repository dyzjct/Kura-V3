package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.Render3DEvent
import dev.kura.net.event.impl.TickEvent
import dev.kura.net.manager.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.util.combat.CrystalUtil
import dev.m7thh4ck.net.util.combat.DamageCalculator
import dev.m7thh4ck.net.util.entity.EntityUtil.aroundBlock
import dev.m7thh4ck.net.util.entity.EntityUtil.isVanished
import dev.m7thh4ck.net.util.extension.sq
import dev.m7thh4ck.net.util.graphics.ESPRenderer
import dev.m7thh4ck.net.util.graphics.easing.Easing
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.math.vector.distanceSqTo
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.PlayerUtil.getHealth
import dev.m7thh4ck.net.util.player.PlayerUtil.getPredictInfo
import dev.m7thh4ck.net.util.world.BlockUtil.fastPos
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object M7thAura : Module("M7thAura", Category.Combat, true) {

    private val page by setting("Page", Page.General)

    // General
    private val targetRange by setting("TargetRange", 12.0f, 0.5f..20.0f, 0.5f) { page == Page.Calculation }
    private val targetPriority by setting("TargetPriority", TargetPriority.Distance) { page == Page.General }

    // Calculation
    private val motionPredict by setting("MotionPredict", true) { page == Page.Calculation }
    private val predictTicks by setting("PredictTicks", 8, 0..20, 1) { page == Page.Calculation && motionPredict }
    private val eatingPause by setting("EatingPause", false) { page == Page.Calculation }
    private val rotate by setting("Rotate", true) { page == Page.General }
    private val antiSuicide by setting("AntiSuicide", true) { page == Page.General }
    private val autoSwap by setting("AutoSwap", AutoSwap.None) { page == Page.General }
    private val silentBypass by setting(
        "SilentBypass",
        SilentBypass.Pick
    ) { page == Page.General && autoSwap == AutoSwap.Silent }

    // Place
    private val placeDelay by setting("PlaceDelay", 50, 0..1000, 5) { page == Page.Place }
    private val placeRange by setting("PlaceRange", 5.0f, 0.0f..8.0f, 0.1f) { page == Page.Place }
    private val placeMinDmg by setting("PlaceMinDmg", 5.0f, 0.0f..20.0f, 0.25f) { page == Page.Place }
    private val placeMaxSelfDmg by setting("PlaceMaxSelfDmg", 12.0f, 0.0f..20.0f, 0.25f) { page == Page.Place }

    // Break
    private val syncHit by setting("SyncHit", true) { page == Page.Break }
    private val breakDelay by setting("BreakDelay", 50, 0..1000, 5) { page == Page.Break }
    private val frequency by setting("Frequency", 20, 5..20, 1) { page == Page.Break }
    private val breakRange by setting("BreakRange", 5.0f, 0.0f..8.0f, 0.1f) { page == Page.Break }
    private val breakMinDmg by setting("BreakMinDmg", 5.0f, 0.0f..20.0f, 0.25f) { page == Page.Break }
    private val breakMaxSelfDmg by setting("BreakMaxSelfDmg", 12.0f, 0.0f..20.0f, 0.25f) { page == Page.Break }

    // Render
    private val red by setting("Red", 255, 0..255, 1) { page == Page.Render }
    private val green by setting("Green", 188, 0..255, 1) { page == Page.Render }
    private val blue by setting("Blue", 255, 0..255, 1) { page == Page.Render }
    private val fillAlpha by setting("BoxAlpha", 51, 0..255, 1) { page == Page.Render }
    private val lineAlpha by setting("OutlineAlpha", 153, 0..255, 1) { page == Page.Render }
    private val renderMode by setting("RenderMode", RenderMode.Motion) { page == Page.Render }
    private val movingLength by setting(
        "MovingLength",
        120,
        0..1000,
        1
    ) { page == Page.Render && renderMode == RenderMode.Motion }
    private val fadeLength by
    setting("FadeLength", 1000, 0..1000, 1) { page == Page.Render && renderMode != RenderMode.Normal }

    private var target: PlayerEntity? = null

    private val placeTimer = TimerUtils()
    private val attackTimer = TimerUtils()

    private var crystalList = CopyOnWriteArrayList<EndCrystalEntity>()
    private var placeList = CopyOnWriteArrayList<PlaceInfo>()

    private val renderMap = ConcurrentHashMap<Vec3d, Long>()
    private var currentPos: RenderPos? = null
    private var prevPos: RenderPos? = null
    private var lastRenderPos: RenderPos? = null
    private var lastUpdateTime: Long = 0L
    private var ticksPassed = 0
    private var attacks = 0
    override fun getHudInfo(): String {
        return target?.name?.string?.let { "[$it]" } ?: "[]"
    }

    private fun toRenderBox(vec3d: Vec3d, scale: Float): Box {
        val halfSize = 0.5 * scale
        return Box(
            vec3d.x - halfSize,
            vec3d.y - halfSize,
            vec3d.z - halfSize,
            vec3d.x + halfSize,
            vec3d.y + halfSize,
            vec3d.z + halfSize
        )
    }

    init {
        eventListener<TickEvent.Post> {
            target = null
            if (autoSwap == AutoSwap.None && player.inventory.mainHandStack.item != Items.END_CRYSTAL) {
                return@eventListener
            }

            if (ticksPassed >= 0) {
                ticksPassed--
            } else {
                ticksPassed = 20
                attacks = 0
            }
            if (!searchTarget()) {
                return@eventListener
            }
            if (player.isUsingItem && eatingPause) return@eventListener
            update()
            doRotate()
            doBreak()
            doPlace()
        }
        eventListener<Render3DEvent> { event ->
            if (fullNullCheck()) return@eventListener
            val color = Color(red, green, blue)
            renderMap.forEach { (pos: Vec3d, time: Long) ->
                if (renderMode != RenderMode.Motion && renderMode != RenderMode.Normal) {
                    if (System.currentTimeMillis() - time > fadeLength) {
                        renderMap.remove(pos)
                    } else {
                        val mutScale = Easing.IN_CUBIC.dec(Easing.toDelta(time, fadeLength))
                        val box = toRenderBox(pos, if (renderMode == RenderMode.Scale) mutScale else 1f)
                        val renderer = ESPRenderer()
                        renderer.aFilled = (fillAlpha * mutScale).toInt()
                        renderer.aOutline = (lineAlpha * mutScale).toInt()
                        renderer.add(box, color)
                        renderer.render(event.matrices, false)
                    }
                } else {
                    renderMap.remove(pos)
                }
            }
            prevPos?.let { prev ->
                currentPos?.let {
                    if (renderMode != RenderMode.Scale && renderMode != RenderMode.Fade) {
                        val scale = if (renderMap.none()) {
                            Easing.IN_CUBIC.dec(Easing.toDelta(it.long, fadeLength))
                        } else Easing.OUT_CUBIC.inc(Easing.toDelta(it.long, fadeLength))
                        val multiplier = Easing.OUT_QUART.inc(
                            Easing.toDelta(
                                lastUpdateTime, movingLength
                            )
                        )
                        val motionRenderPos = prev.pos.add(it.pos.subtract(prev.pos).multiply(multiplier.toDouble()))
                        val box = toRenderBox(if (renderMode == RenderMode.Motion) motionRenderPos else it.pos, scale)
                        val renderer = ESPRenderer()

                        renderer.aFilled = (fillAlpha * scale).toInt()
                        renderer.aOutline = (lineAlpha * scale).toInt()
                        renderer.add(box, color)
                        renderer.render(event.matrices, false)
                        lastRenderPos = it
                    }
                }
            }
        }
    }

    private fun doPlace() {
        if (placeList.none()) return
        placeList.first().pos.let { cryPos ->
            InventoryUtil.findItemInHotbar(Items.END_CRYSTAL)?.let { crySlot ->
                currentPos = RenderPos(cryPos.down().toCenterPos(), System.currentTimeMillis())
                prevPos = lastRenderPos ?: currentPos
                renderMap[cryPos.down().toCenterPos()] = System.currentTimeMillis()
                if (placeTimer.tickAndReset(placeDelay)) {
                    when (autoSwap) {
                        AutoSwap.None -> {
                            connection.sendPacket(fastPos(cryPos, strictDirection = true, render = false))
                            swing()
                        }

                        AutoSwap.Switch -> {
                            InventoryUtil.switchTo(crySlot)
                            connection.sendPacket(fastPos(cryPos, strictDirection = true, render = false))
                            swing()
                        }

                        AutoSwap.Silent -> {
                            when (silentBypass) {
                                SilentBypass.Pick -> {
                                    InventoryUtil.spoofHotbar(crySlot) {
                                        connection.sendPacket(fastPos(cryPos, strictDirection = true, render = false))
                                        swing()
                                    }
                                }

                                SilentBypass.Swap -> {
                                    InventoryUtil.spoofHotbarBypass(crySlot) {
                                        connection.sendPacket(fastPos(cryPos, strictDirection = true, render = false))
                                        swing()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun divide(frequency: Int): ArrayList<Int> {
        val freqAttacks = ArrayList<Int>()
        var size = 0
        if (20 < frequency) return freqAttacks else if (20 % frequency == 0) {
            for (i in 0 until frequency) {
                size += 20 / frequency
                freqAttacks.add(size)
            }
        } else {
            val zp = frequency - 20 % frequency
            val pp = 20 / frequency
            for (i in 0 until frequency) {
                if (i >= zp) {
                    size += pp + 1
                    freqAttacks.add(size)
                } else {
                    size += pp
                    freqAttacks.add(size)
                }
            }
        }
        return freqAttacks
    }

    private fun doBreak() {
        if (crystalList.none()) return
        crystalList.first().let { cry ->
            if (attackTimer.tickAndReset(breakDelay) || !(!divide(frequency).contains(ticksPassed)) ) {
                world.entities.forEach {
                    if (it is EndCrystalEntity && it.boundingBox.intersects(cry.boundingBox)) {
                        if (rotate) RotationManager.addRotation(cry.blockPos, 3)
                        if (syncHit) {
                            player.networkHandler.sendPacket(
                                PlayerInteractEntityC2SPacket.interact(
                                    it,
                                    player.isSneaking,
                                    Hand.MAIN_HAND
                                )
                            )
                        }
                        player.networkHandler.sendPacket(
                            PlayerInteractEntityC2SPacket.attack(
                                it,
                                player.isSneaking
                            )
                        )
                        attacks++
                        swing()
                        return
                    }
                }
            }

        }
    }

    private fun doRotate() {
        if (!rotate) return
        if (placeList.none() && !crystalList.none()) {
            RotationManager.addRotation(crystalList.first().pos.add(0.0, -0.5, 0.0), 3)
        } else if (!placeList.none()) {
            RotationManager.addRotation(placeList.first().pos, 3)
        }
    }

    private fun update() {
        findCrystalsList()?.let {
            crystalList = CopyOnWriteArrayList(it)
        }
        findPlaceList().let {
            placeList = CopyOnWriteArrayList(it)
        }
    }

    private fun findCrystalsList(): List<EndCrystalEntity>? {
        target?.let { t ->
            val predict = t.getPredictInfo(predictTicks)
            return world.entities.asSequence().filterIsInstance<EndCrystalEntity>().filter {
                player.distanceSqTo(it.pos) <= breakRange.sq
            }.filter {
                DamageCalculator.crystalDamage(
                    t,
                    if (motionPredict) predict.pos else t.pos,
                    if (motionPredict) predict.box else t.boundingBox,
                    it.blockPos
                ) >= breakMinDmg
            }.filter {
                !antiSuicide || DamageCalculator.crystalDamage(
                    player,
                    player.pos,
                    player.boundingBox,
                    it.blockPos
                ) < getHealth()
            }.filter {
                DamageCalculator.crystalDamage(
                    player,
                    player.pos,
                    player.boundingBox,
                    it.blockPos
                ) <= breakMaxSelfDmg
            }.toList().sortedByDescending {
                DamageCalculator.crystalDamage(
                    t,
                    if (motionPredict) predict.pos else t.pos,
                    if (motionPredict) predict.box else t.boundingBox,
                    it.blockPos
                )
            }
        }
        return null
    }

    private fun findPlaceList(): List<PlaceInfo> {
        target?.let { tgt ->
            val list = CopyOnWriteArrayList<PlaceInfo>()
            lastUpdateTime = System.currentTimeMillis()
            val blocks = tgt.blockPos.aroundBlock(10)
                .filter { player.squaredDistanceTo(it.toCenterPos()) <= placeRange.sq }
                .filter { CrystalUtil.canPlaceCrystal(it) }

            val predict = tgt.getPredictInfo(predictTicks)

            for (block in blocks) {
                val crystalPos = block.up()
                val selfDamage = DamageCalculator.crystalDamage(
                    player, player.pos, player.boundingBox, crystalPos
                )
                if (selfDamage <= placeMaxSelfDmg) {

                    if (antiSuicide && selfDamage >= getHealth()) continue

                    if (!world.isAir(crystalPos)) continue

                    if (DamageCalculator.crystalDamage(
                            tgt, if (motionPredict) predict.pos else tgt.pos, predict.box, crystalPos
                        ) < placeMinDmg
                    ) continue

                    list.add(
                        PlaceInfo(
                            crystalPos,
                            DamageCalculator.crystalDamage(
                                tgt, if (motionPredict) predict.pos else tgt.pos, predict.box, crystalPos
                            )
                        )
                    )
                }
            }
            list.sortByDescending { it.dmg }
            return list
        }
        return emptyList()
    }


    private fun searchTarget(): Boolean {
        target?.let {
            if (it.isVanished() ||
                player.distanceTo(it) > targetRange
            ) target = null
            return true
        }
        val resultMap = mutableMapOf<PlayerEntity, Float>()
        world.entities
            .filterIsInstance<PlayerEntity>()
            .filter { !it.isVanished() }
            .filter { player.squaredDistanceTo(it) <= targetRange * targetRange }
            .also {
                if (it.isEmpty()) return false
            }
            .forEach {
                synchronized(it) {
                    resultMap[it] = targetPriority.func.invoke(it)
                }
            }
        target = resultMap.entries.sortedBy { it.value }[0].key
        return true
    }

    private data class PlaceInfo(val pos: BlockPos, val dmg: Double)

    private data class RenderPos(val pos: Vec3d, var long: Long)

    private enum class Page(override val displayName: CharSequence) : DisplayEnum {
        General("General"),
        Calculation("Calculation"),
        Place("Place"),
        Break("Break"),
        Render("Render")
    }

    private enum class AutoSwap(override val displayName: CharSequence) : DisplayEnum {
        None("None"),
        Switch("Switch"),
        Silent("Silent")
    }

    private enum class SilentBypass(override val displayName: CharSequence) : DisplayEnum {
        Swap("Swap"),
        Pick("Pick")
    }

    private enum class RenderMode(override val displayName: CharSequence) : DisplayEnum {
        Normal("Normal"),
        Motion("Motion"),
        Fade("Fade"),
        Scale("Scale")
    }

    @Suppress("unused")
    private enum class TargetPriority(
        override val displayName: CharSequence,
        val func: (PlayerEntity) -> Float
    ) : DisplayEnum {

        Distance("Distance", {
            player.distanceTo(it)
        }),
        Health("Health", {
            it.health + it.absorptionAmount
        }),
        ArmorDamage("ArmorDamage", { entity ->
            entity.armorItems.filter { it.isDamageable }.sumOf { it.damage }.toFloat()
        })
    }

}