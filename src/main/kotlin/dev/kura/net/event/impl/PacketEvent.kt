package dev.kura.net.event.impl

import dev.kura.net.event.CancellableEvent
import net.minecraft.network.packet.Packet

sealed class PacketEvent(val packet: Packet<*>) : CancellableEvent() {
    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)
    class ReceivePost(packet: Packet<*>) : PacketEvent(packet)
    class SendPost(packet: Packet<*>) : PacketEvent(packet)
}