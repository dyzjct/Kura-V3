package dev.kura.net.utils.other

import dev.kura.net.mod.gui.screen.ClickGUIScreen
import dev.kura.net.utils.Util
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen
import net.minecraft.client.gui.screen.ingame.AnvilScreen
import net.minecraft.client.gui.screen.ingame.SignEditScreen
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen

object ScreenUtils : Util() {
    val Screen?.isGui: Boolean
        get() = this is ClickGUIScreen

    fun Screen.notWhiteListScreen(): Boolean {
        return this is ChatScreen
                || this is SignEditScreen
                || this is AnvilScreen
                || this is AbstractCommandBlockScreen
                || this is StructureBlockScreen
    }

    fun Screen?.safeReturn(): Boolean {
        return this != null && this.notWhiteListScreen() || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null || this.isGui
    }
}