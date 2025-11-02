package dev.m7thh4ck.net.mod.module.impl.client

import com.mojang.blaze3d.platform.GlStateManager
import dev.m7thh4ck.net.M7thH4ck
import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.Render2DEvent
import dev.m7thh4ck.net.managers.impl.ModuleManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.graphics.Render2DEngine
import dev.m7thh4ck.net.util.graphics.TextUtil
import net.minecraft.item.ItemStack
import net.minecraft.world.dimension.DimensionTypes
import java.awt.Color

object HUD : Module("HUD", Category.Client) {

    val shadow by setting("Shadow", true)

    // WaterMark
    private val waterMark by setting("WaterMark", true)
    private val version by setting("Version", false) { waterMark }
    private val waterMarkX by setting("WaterMarkX", 75, 0..500, 1) { waterMark }
    private val waterMarkY by setting("WaterMarkY", 75, 0..500, 1) { waterMark }
    private val waterMarkScale by setting("WaterMarkScale", 1.0f, 1.0f..3.0f, 0.1f) { waterMark }

    // CoordsHud
    private val coords by setting("Coords", true)

    // ArrayList
    private val arrayList by setting("ArrayList", true)
    private val damage by setting("Armour", true)
    private val MarkX by setting("ArmourMarkX", 360, 0..500, 1) { damage }
    private val MarkY by setting("ArmourMarkY", 490, 0..500, 1) { damage }
    val arrayListY by setting("ArrayListY", 30, 0..500, 5) { arrayList }
    private val rainbow by setting("Rainbow", false)
    private val colorRed by setting("Red", 80, 0..255, 1) { !rainbow }
    private val colorGreen by setting("Green", 150, 0..255, 1) { !rainbow }
    private val colorBlue by setting("Blue", 255, 0..255, 1) { !rainbow }
    private val speed by setting("Speed", 18, 2..54, 1) { rainbow }
    private val saturation by setting("Saturation", 0.65f, 0.0f..1.0f, 0.01f) { rainbow }
    private val brightness by setting("Brightness", 1.0f, 0.0f..1.0f, 0.01f) { rainbow }
    var width = 80f
    var height
        get() = if (damage) MarkY
        else 15f
        set(value) {}

    init {
        eventListener<Render2DEvent> { event ->

            if (waterMark) {
                if (version) TextUtil.drawStringWithScale(
                    event.context,
                    M7thH4ck.NAME + " v" + M7thH4ck.VERSION,
                    waterMarkX.toFloat(), waterMarkY.toFloat(),
                    getColor(), shadow, waterMarkScale
                )
                else TextUtil.drawStringWithScale(
                    event.context,
                    M7thH4ck.NAME,
                    waterMarkX.toFloat(), waterMarkY.toFloat(),
                    getColor(), shadow, waterMarkScale
                )

            }

            if (coords) {
                TextUtil.drawStringWithScale(
                    event.context,
                    getCoords(),
                    2.0f,
                    mc.window.scaledHeight - 10.0f,
                    getColor(),
                    shadow,
                    1f
                )
            }
            if (damage) {
                var iteration = 0
                for (item in mc.player?.inventory?.armor ?: listOf<ItemStack>()) {
                    ++iteration
                    if (item.isEmpty) continue
                    val xPos = MarkX - 90 + (9 - iteration) * 20 - 8

                    val offsetY = if (damage) MarkY
                    else 0
                    var index = 0
                    GlStateManager._enableDepthTest()
                    event.context.drawItem(item, xPos, (90 + offsetY))
                    event.context.drawItemInSlot(mc.textRenderer, item, xPos, (90 + offsetY))
                    GlStateManager._disableDepthTest()
                    val itemCount = item.count.toString()
                    TextUtil.drawString(
                        event.context, itemCount,
                        mc.window.scaledWidth - mc.textRenderer.getWidth(itemCount) - 4f,
                        MarkY + 4f + (index * mc.textRenderer.fontHeight),
                        getColor(), shadow
                    )
                    if (damage) {
                        //     drawDamage(event.context, item, xPos.toInt(), HUD.arrayListY + 4f)
                    }
                }
            }
            if (arrayList) {
                var index = 0
                ModuleManager.getEnabledModules()
                    .filter { it.drawn }
                    .sortedWith(Comparator.comparingInt<Module> { mc.textRenderer.getWidth(it.name) }.reversed())
                    .forEach { mod ->
                        if (mod.hudInfo) {
                            val info = "[${mod.getHudInfo()}]"

                            TextUtil.drawString(
                                event.context, mod.name,
                                mc.window.scaledWidth - mc.textRenderer.getWidth(mod.name + info) - 4f,
                                arrayListY + 4f + (index * mc.textRenderer.fontHeight),
                                getColor(), shadow
                            )

                            TextUtil.drawString(
                                event.context, info,
                                mc.window.scaledWidth - mc.textRenderer.getWidth(info) - 4f,
                                arrayListY + 4f + (index * mc.textRenderer.fontHeight),
                                getColor(), shadow
                            )

                        } else {
                            TextUtil.drawString(
                                event.context, mod.name,
                                mc.window.scaledWidth - mc.textRenderer.getWidth(mod.name) - 4f,
                                arrayListY + 4f + (index * mc.textRenderer.fontHeight),
                                getColor(), shadow
                            )

                        }
                        ++index
                    }
            }
        }
    }

    private fun getCoords(): String {
        var s = 0.125
        val isNether = player.world.dimensionKey === DimensionTypes.THE_NETHER
        if (isNether) s = 8.0
        val worldPos = java.lang.String.format("X: %.1f,Y: %.1f,Z: %.1f", player.x, player.y, player.z)
        val otherWorld =
            java.lang.String.format("X: %.1f,Y: %.1f,Z: %.1f", player.x * s, player.y * s, player.z * s)
        var f = "Coords: $worldPos Nether:$otherWorld"
        if (isNether) f = "Coords: $otherWorld OverWorld:$worldPos"
        return f
    }


    fun getColor(): Color {
        return if (rainbow) {
            Render2DEngine.rainbow(speed, 0, saturation, brightness, 1f)
        } else {
            Color(colorRed, colorGreen, colorBlue)
        }
    }
}