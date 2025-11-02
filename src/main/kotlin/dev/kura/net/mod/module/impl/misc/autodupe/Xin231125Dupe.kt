package dev.kura.net.mod.module.impl.misc.autodupe

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.DeathScreen
import net.minecraft.text.Text

object Xin231125Dupe {

    private val mc = MinecraftClient.getInstance()!!
    private val world = mc.world!!
    private val player = mc.player!!

    fun onTick() {
        if (mc.currentScreen is DeathScreen) {
            player.requestRespawn()
            mc.setScreen(null)
        }

        player.sendMessage(Text.of("/kill"))
    }

}