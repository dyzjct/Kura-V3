package dev.kura.net.mod.gui.click.impl

import dev.kura.net.mod.gui.click.Component
import dev.kura.net.mod.gui.click.ModuleButton
import dev.m7thh4ck.net.mod.module.impl.client.ClickGUI
import dev.kura.net.settings.AbstractSetting
import dev.kura.net.settings.BooleanSetting
import dev.m7thh4ck.net.util.graphics.Render2DEngine
import dev.m7thh4ck.net.util.graphics.TextUtil
import dev.m7thh4ck.net.util.graphics.color.ColorRGB
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.awt.Color

class CheckBox(setting: AbstractSetting<*>, parent: ModuleButton, offset: Int) : Component(setting, parent, offset) {

    private val boolSet: BooleanSetting = setting as BooleanSetting

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!boolSet.visibility.invoke()) return

        val mc = MinecraftClient.getInstance() ?: return

        if (isHovered(mouseX.toDouble(), mouseY.toDouble())) {
            Render2DEngine.drawRect(
                context.matrices, parent.parent.x.toFloat(), parent.parent.y.toFloat() + parent.offset + offset,
                parent.parent.width.toFloat(), parent.parent.height.toFloat(),
                if (!boolSet.value) Color(
                    ClickGUI.getColorRect().red,
                    ClickGUI.getColorRect().green,
                    ClickGUI.getColorRect().blue,
                    30
                )
                else Color(ClickGUI.getColor().red, ClickGUI.getColor().green, ClickGUI.getColor().blue, 220)
            )
        } else {
            Render2DEngine.drawRect(
                context.matrices, parent.parent.x.toFloat(), parent.parent.y.toFloat() + parent.offset + offset,
                parent.parent.width.toFloat(), parent.parent.height.toFloat(),
                if (!boolSet.value) Color(
                    ClickGUI.getColorRect().red,
                    ClickGUI.getColorRect().green,
                    ClickGUI.getColorRect().blue,
                    60
                )
                else Color(ClickGUI.getColor().red, ClickGUI.getColor().green, ClickGUI.getColor().blue, 200)
            )
        }

        val textOffset = (parent.parent.height / 2) - mc.textRenderer.fontHeight / 2

        if (ClickGUI.outline) Render2DEngine.drawRectOutline(
            context.matrices, parent.parent.x.toFloat(), parent.parent.y.toFloat() + parent.offset + offset,
            parent.parent.width.toFloat(), parent.parent.height.toFloat(), ClickGUI.getColorLine()
        )

        TextUtil.drawString(
            context, boolSet.displayName.toString(),
            parent.parent.x + textOffset.toFloat(), parent.parent.y + parent.offset + offset + textOffset.toFloat(),
            ColorRGB(255, 255, 255), ClickGUI.shadow
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (!boolSet.visibility.invoke()) return
        if (isHovered(mouseX, mouseY) && button == 0) {
            boolSet.value = !boolSet.value
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (!boolSet.visibility.invoke()) return
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        if (!boolSet.visibility.invoke()) return
    }

}
