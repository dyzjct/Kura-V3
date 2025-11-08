package dev.m7thh4ck.net.util

import net.minecraft.client.MinecraftClient


open class Util {
    val mc: MinecraftClient get() = MinecraftClient.getInstance()!!
    val player get() = mc.player!!
    val world get() = mc.world!!
}