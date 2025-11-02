package dev.kura.net.mod.gui.screen

import dev.kura.net.mod.gui.click.ClickGUIFrame
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.impl.client.ClickGUI
import dev.kura.net.utils.graphics.Render2DEngine
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color

object ClickGUIScreen : Screen(Text.literal("ClickGUI")) {

    private val frames: MutableList<ClickGUIFrame>

    init {
        frames = arrayListOf()

        var offset = 20
        for (category in Category.entries) {
            frames.add(ClickGUIFrame(category, offset, 20, 100, 14))
            offset += 125
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val mc = MinecraftClient.getInstance() ?: return

        if (ClickGUI.background) Render2DEngine.drawRect(
            context.matrices, 0f, 0f,
            mc.window.scaledWidth.toFloat(), mc.window.scaledHeight.toFloat(), Color(
                ClickGUI.backRed,
                ClickGUI.backGreen,
                ClickGUI.backBlue, ClickGUI.backA
            )
        )

        frames.forEach {
            it.render(context, mouseX, mouseY, delta)
            it.updatePosition(mouseX, mouseY)
        }
//        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        frames.forEach { it.mouseClicked(mouseX, mouseY, button) }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        frames.forEach { it.mouseReleased(mouseX, mouseY, button) }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        frames.forEach { it.mouseScrolled(mouseX, mouseY, amount) }
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        frames.forEach { it.keyReleased(keyCode, scanCode, modifiers) }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun close() {
        ClickGUI.disable()
        super.close()
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun shouldCloseOnEsc(): Boolean {
        return true
    }

}