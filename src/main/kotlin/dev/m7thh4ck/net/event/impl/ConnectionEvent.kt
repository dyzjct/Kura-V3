package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.CancellableEvent
import net.minecraft.client.network.ServerAddress
import net.minecraft.client.network.ServerInfo

sealed class ConnectionEvent : CancellableEvent() {
    data class Join(val serverInfo: ServerInfo, val serverAddress: ServerAddress) : ConnectionEvent()
    data object Disconnect : ConnectionEvent()
}