package dev.kura.net.utils

import net.minecraft.client.MinecraftClient

open class Util {
    val mc: MinecraftClient get() = MinecraftClient.getInstance()!!
    val player get() = mc.player!!
    val world get() = mc.world!!
}