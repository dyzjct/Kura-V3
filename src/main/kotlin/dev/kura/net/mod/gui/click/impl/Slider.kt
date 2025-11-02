package dev.kura.net.mod.gui.click.impl

import dev.kura.net.settings.AbstractNumberSettings
import dev.kura.net.settings.AbstractSetting
import dev.kura.net.settings.DoubleSetting
import dev.kura.net.settings.FloatSetting
import dev.kura.net.settings.IntSetting
import dev.kura.net.mod.gui.click.Component
import dev.kura.net.mod.gui.click.ModuleButton
import dev.kura.net.mod.module.impl.client.ClickGUI
import dev.kura.net.utils.graphics.Render2DEngine
import dev.kura.net.utils.graphics.TextUtil
import dev.kura.net.utils.graphics.color.ColorRGB
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Slider(setting: AbstractSetting<*>, parent: ModuleButton, offset: Int) : Component(setting, parent, offset) {

    private val numSet: AbstractNumberSettings<*> = setting as AbstractNumberSettings<*>

    private var sliding = false

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!numSet.visibility.invoke()) return

        val mc = MinecraftClient.getInstance() ?: return

        val textOffset = (parent.parent.height / 2) - mc.textRenderer.fontHeight / 2

        var stringValue = ""
        when (numSet) {
            is IntSetting -> {
                stringValue = numSet.value.toString()
            }

            is FloatSetting -> {
                stringValue = roundToPlace(numSet.value.toDouble(), 2).toString()
            }

            is DoubleSetting -> {
                stringValue = roundToPlace(numSet.value, 2).toString()
            }
        }

        val diff = min(parent.parent.width, max(0, mouseX - parent.parent.x))

        if (sliding) {
            if (diff == 0) {
                when (numSet) {
                    is IntSetting -> {
                        numSet.value = numSet.minValue
                    }

                    is FloatSetting -> {
                        numSet.value = numSet.minValue
                    }

                    is DoubleSetting -> {
                        numSet.value = numSet.minValue
                    }
                }
            } else {
                when (numSet) {
                    is IntSetting -> {
                        val value =
                            floor(((diff.toFloat() / parent.parent.width.toFloat()) * (numSet.maxValue - numSet.minValue) + numSet.minValue) / numSet.step) * numSet.step
                        numSet.value = value.toInt()
                    }

                    is FloatSetting -> {
                        val value =
                            floor(((diff.toFloat() / parent.parent.width.toFloat()) * (numSet.maxValue - numSet.minValue) + numSet.minValue) / numSet.step) * numSet.step
                        numSet.value = value
                    }

                    is DoubleSetting -> {
                        val value =
                            floor(((diff.toFloat() / parent.parent.width.toFloat()) * (numSet.maxValue - numSet.minValue) + numSet.minValue) / numSet.step) * numSet.step
                        numSet.value = value
                    }
                }
            }
        }


        var renderWidth = (parent.parent.width * (numSet.value.toFloat() - numSet.minValue.toFloat())
                / (numSet.maxValue.toFloat() - numSet.minValue.toFloat()))

        if (numSet.value.toFloat() > numSet.maxValue.toFloat()) renderWidth = parent.parent.width.toFloat()

        if (isHovered(mouseX.toDouble(), mouseY.toDouble())) {
            Render2DEngine.drawRect(
                context.matrices,
                parent.parent.x.toFloat(),
                parent.parent.y.toFloat() + parent.offset + offset,
                parent.parent.width.toFloat(),
                parent.parent.height.toFloat(),
                Color(ClickGUI.getColorRect().red, ClickGUI.getColorRect().green, ClickGUI.getColorRect().blue, 30)
            )
        } else {
            Render2DEngine.drawRect(
                context.matrices,
                parent.parent.x.toFloat(),
                parent.parent.y.toFloat() + parent.offset + offset,
                parent.parent.width.toFloat(),
                parent.parent.height.toFloat(),
                Color(ClickGUI.getColorRect().red, ClickGUI.getColorRect().green, ClickGUI.getColorRect().blue, 60)
            )
        }

        Render2DEngine.drawRect(
            context.matrices,
            parent.parent.x.toFloat(),
            parent.parent.y.toFloat() + parent.offset + offset,
            renderWidth,
            parent.parent.height.toFloat(),
            Color(ClickGUI.getColor().red, ClickGUI.getColor().green, ClickGUI.getColor().blue, 200)
        )


        if (ClickGUI.outline) Render2DEngine.drawRectOutline(
            context.matrices, parent.parent.x.toFloat(), parent.parent.y.toFloat() + parent.offset + offset,
            parent.parent.width.toFloat(), parent.parent.height.toFloat(), ClickGUI.getColorLine()
        )


        TextUtil.drawString(
            context, "${numSet.displayName}: $stringValue",
            parent.parent.x + textOffset.toFloat(), parent.parent.y + parent.offset + offset + textOffset.toFloat(),
            ColorRGB(255, 255, 255), ClickGUI.shadow
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (!numSet.visibility.invoke()) return
        if (isHovered(mouseX, mouseY)) sliding = true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (!numSet.visibility.invoke()) return
        sliding = false
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        if (!numSet.visibility.invoke()) return
    }

    private fun roundToPlace(value: Double, place: Int): Double {
        if (place < 0) return value

        var bd = BigDecimal(value)
        bd = bd.setScale(place, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

}