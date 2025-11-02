package dev.kura.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.event.impl.TickEvent
import dev.kura.net.manager.impl.FriendManager
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.kura.net.utils.helper.ChatUtil
import net.minecraft.entity.EntityStatuses
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
import java.util.concurrent.ConcurrentHashMap

object TotemPopCounter : Module(
    name = "TotemPopCounter",
    category = Category.Combat
) {
    private var playerList = ConcurrentHashMap<PlayerEntity, Int>()

    init {
        eventListener<PacketEvent.Receive> { event ->
            if (event.packet is EntityStatusS2CPacket) {
                val players = event.packet.getEntity(world)
                if (event.packet.status == EntityStatuses.USE_TOTEM_OF_UNDYING && players is PlayerEntity) {
                    if (playerList.containsKey(players)) {
                        playerList[players]?.let {
                            playerList[players] = it + 1
                        }
                    } else {
                        playerList[players] = 1
                    }

                    val name = players.name.string
                    val pop = playerList[players]
                    if (players.isAlive) {
                        if (FriendManager.isFriend(name) && player != players) {
                            ChatUtil.sendRawMessage("Your Friend $name Popped ${ChatUtil.colorKANJI}$pop Totem!")
                        } else if (player == players) {
                            ChatUtil.sendRawMessage("I Popped ${ChatUtil.colorKANJI}$pop Totem!")
                        } else {
                            ChatUtil.sendRawMessage("$name Popped ${ChatUtil.colorKANJI}$pop Totem!")
                        }
                    }
                }
            }
        }

        eventListener<TickEvent.Post> {
            playerList.forEach {
                if (!world.entities.filterIsInstance<PlayerEntity>().contains(it.key)) {
                    ChatUtil.sendRawMessage("${it.key.entityName} died after popping ${it.value} Totems!")
                    playerList.remove(it.key)
                }
            }
        }
    }
}