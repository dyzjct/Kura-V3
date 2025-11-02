package dev.kura.net.mod.gui.click

import dev.kura.net.mod.gui.click.impl.BindBox
import dev.kura.net.mod.gui.click.impl.CheckBox
import dev.kura.net.mod.gui.click.impl.ModeBox
import dev.kura.net.mod.gui.click.impl.Slider
import dev.kura.net.mod.module.Module
import dev.kura.net.mod.module.impl.client.ClickGUI
import dev.kura.net.settings.AbstractNumberSettings
import dev.kura.net.settings.BooleanSetting
import dev.kura.net.settings.EnumSetting
import dev.kura.net.settings.KeyBindSetting
import dev.kura.net.utils.graphics.Render2DEngine
import dev.kura.net.utils.graphics.TextUtil
import dev.kura.net.utils.graphics.color.ColorRGB
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.awt.Color

class ModuleButton(val module: Module, val parent: ClickGUIFrame, var offset: Int) {

    private val mc = MinecraftClient.getInstance()
    val components: MutableList<Component> = arrayListOf()
    var extended: Boolean = false

    init {
        var setOffset = parent.height
        for (setting in module.getSettings()) {
            when (setting) {
                is AbstractNumberSettings -> {
                    components.add(Slider(setting, this, setOffset))
                    setOffset += parent.height
                }

                is BooleanSetting -> {
                    components.add(CheckBox(setting, this, setOffset))
                    setOffset += parent.height
                }

                is EnumSetting<*> -> {
                    components.add(ModeBox(setting, this, setOffset))
                    setOffset += parent.height
                }

                is KeyBindSetting -> {
                    components.add(BindBox(setting, this, setOffset))
                    setOffset += parent.height
                }
            }
        }
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (isHovered(mouseX.toDouble(), mouseY.toDouble())) {
            Render2DEngine.drawRect(
                context.matrices,
                parent.x.toFloat(),
                parent.y.toFloat() + offset,
                parent.width.toFloat(),
                parent.height.toFloat(),
                Color(ClickGUI.getColorRect().red, ClickGUI.getColorRect().green, ClickGUI.getColorRect().blue, 60)
            )
        } else {
            Render2DEngine.drawRect(
                context.matrices,
                parent.x.toFloat(),
                parent.y.toFloat() + offset,
                parent.width.toFloat(),
                parent.height.toFloat(),
                Color(ClickGUI.getColorRect().red, ClickGUI.getColorRect().green, ClickGUI.getColorRect().blue, 80)
            )
        }

        if (ClickGUI.outline) Render2DEngine.drawRectOutline(
            context.matrices,
            parent.x.toFloat(),
            parent.y.toFloat() + offset,
            parent.width.toFloat(),
            parent.height.toFloat(),
            ClickGUI.getColorLine()
        )

        val textOffset = (parent.height / 2) - mc.textRenderer.fontHeight / 2

        TextUtil.drawString(
            context, module.name,
            parent.x + textOffset.toFloat(), parent.y + offset + textOffset.toFloat(),
            if (!module.isEnabled()) ColorRGB(255, 255, 255)
            else ColorRGB(ClickGUI.getColor().red, ClickGUI.getColor().green, ClickGUI.getColor().blue),
            ClickGUI.shadow
        )


        TextUtil.drawString(
            context, if (extended) "-" else "+",
            parent.x + parent.width - mc.textRenderer.getWidth("+") - ((parent.height / 2.0f) - mc.textRenderer.fontHeight / 2.0f),
            parent.y + offset + textOffset.toFloat(),
            if (!module.isEnabled()) ColorRGB(255, 255, 255)
            else ColorRGB(ClickGUI.getColor().red, ClickGUI.getColor().green, ClickGUI.getColor().blue),
            ClickGUI.shadow
        )

        if (extended) {
            refreshComponentsOffset()
            components.forEach { it.render(context, mouseX, mouseY, delta) }
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                module.toggle()
            } else {
                extended = !extended
                parent.updateButtons()
            }
        }

        if (extended) {
            components.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (extended) {
            components.forEach { it.mouseReleased(mouseX, mouseY, button) }
        }
    }

    private fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > parent.x && mouseX < (parent.x + parent.width)
                && mouseY > parent.y + offset && mouseY < (parent.y + offset + parent.height)
    }

    private fun refreshComponentsOffset() {
        var setOffset = parent.height
        for (comp in components) {
            if (!comp.setting.visibility.invoke()) continue
            comp.offset = setOffset
            setOffset += parent.height
        }
    }

    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        components.forEach { it.keyReleased(keyCode, scanCode, modifiers) }
    }

}