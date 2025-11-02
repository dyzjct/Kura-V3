package dev.m7thh4ck.net.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld

object Wrapper {

    val mc: MinecraftClient get() = MinecraftClient.getInstance()!!
    val player: ClientPlayerEntity get() = mc.player!!
    val world: ClientWorld get() = mc.world!!

    val connection get() = mc.networkHandler!!

    val playerController get() = mc.interactionManager!!

    fun fullNullCheck(): Boolean = mc.player == null || mc.world == null

}