package dev.kura.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import io.netty.buffer.Unpooled
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround

object Criticals : Module(
    name = "Criticals",
    category = Category.Combat,
) {
    override fun getHudInfo(): String {
     return "Packet"
    }
    init {
        eventListener<PacketEvent.Send> { event ->
            if (event.packet is PlayerInteractEntityC2SPacket) {
                if (!(getInteractType(event.packet) == InteractType.ATTACK && getEntity(event.packet) is LivingEntity)) return@eventListener
                if (getEntity(event.packet) is EndCrystalEntity) return@eventListener

                doCritical()

            }
        }
    }

    fun doCritical() {
        if (player.isInLava || player.isTouchingWater) return
        val posX = player.x
        val posY = player.y
        val posZ = player.z
        player.networkHandler.sendPacket(PositionAndOnGround(posX, posY + 0.1, posZ, false))
        player.networkHandler.sendPacket(PositionAndOnGround(posX, posY, posZ, false))
    }
    private fun getEntity(packet: PlayerInteractEntityC2SPacket): Entity? {
        val packetBuf = PacketByteBuf(Unpooled.buffer())
        packet.write(packetBuf)
        return world.getEntityById(packetBuf.readVarInt())
    }
    private fun getInteractType(packet: PlayerInteractEntityC2SPacket): InteractType? {
        val packetBuf = PacketByteBuf(Unpooled.buffer())
        packet.write(packetBuf)
        packetBuf.readVarInt()
        return packetBuf.readEnumConstant(InteractType::class.java)
    }
    enum class InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }
}