package dev.kura.net.mod.gui.click

import dev.kura.net.event.impl.ModuleManager
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.impl.client.ClickGUI
import dev.kura.net.utils.graphics.Render2DEngine.drawRect
import dev.kura.net.utils.graphics.Render2DEngine.drawRectOutline
import dev.kura.net.utils.graphics.TextUtil
import dev.kura.net.utils.graphics.color.ColorRGB
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.awt.Color

class ClickGUIFrame(val category: Category, var x: Int, var y: Int, var width: Int, var height: Int) {

    private var dragging = false
    private var extended = true
    private var dragX = 0
    private var dragY = 0

    private val mc = MinecraftClient.getInstance()

    private val moduleButtons: MutableList<ModuleButton> = mutableListOf()

    init {
        var offset = height

        ModuleManager.sortModules()
        ModuleManager.sortedModules
            .filter { it.category == category }
            .forEach {
                moduleButtons.add(ModuleButton(it, this, offset))
                offset += height
            }
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        drawRect(
            context.matrices,
            x.toFloat()-4,
            y.toFloat(),
            width.toFloat()+8,
            height.toFloat(),
            Color(ClickGUI.getColorPanel().red, ClickGUI.getColorPanel().green, ClickGUI.getColorPanel().blue, 100)
        )
        if (ClickGUI.panelLine) drawRectOutline(
            context.matrices,
            x.toFloat()-4,
            y.toFloat(),
            width.toFloat()+8,
            height.toFloat(),
            ClickGUI.getColorPanel()
        )

        val offset = (height / 2) - mc.textRenderer.fontHeight / 2

        TextUtil.drawString(
            context, category.displayString,
            x + (width / 2.0f - mc.textRenderer.getWidth(category.displayString) / 2), y + offset.toFloat(),
            ColorRGB(255, 255, 255), ClickGUI.shadow
        )

        TextUtil.drawString(
            context, if (extended) "-" else "+",
            x + width - (offset) - mc.textRenderer.getWidth("+").toFloat(), y + offset.toFloat(),
            ColorRGB(255, 255, 255), ClickGUI.shadow
        )

        if (extended) {
            updateButtons()
            moduleButtons.forEach { it.render(context, mouseX, mouseY, delta) }
        }

    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (isHovered(mouseX, mouseY)) {
            when (button) {
                0 -> {  // 左键
                    dragging = true
                    dragX = (mouseX - x).toInt()
                    dragY = (mouseY - y).toInt()
                }

                1 -> {  // 右键
                    extended = !extended
                }
            }

        }
        if (extended) {
            moduleButtons.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (button == 0 && dragging) dragging = false
        moduleButtons.forEach { it.mouseReleased(mouseX, mouseY, button) }
    }

    fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double) {
        y += amount.toInt() * 4
    }

    private fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > x && mouseX < (x + width) && mouseY > y && mouseY < (y + height)
    }

    fun updatePosition(mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - dragX
            y = mouseY - dragY
        }
    }

    fun updateButtons() {
        var offset = height

        for (modButton in moduleButtons) {
            modButton.offset = offset

            if (modButton.extended) {
//                for (comp in modButton.components) {
//                    if (comp.setting.isVisible.invoke()) offset += height
//                }
                modButton.components
                    .filter { it.setting.isVisible.invoke() }
                    .forEach { _ ->
                        offset += height
                    }
            }

            offset += height
        }
    }

    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        moduleButtons.forEach { it.keyReleased(keyCode, scanCode, modifiers) }
    }

}