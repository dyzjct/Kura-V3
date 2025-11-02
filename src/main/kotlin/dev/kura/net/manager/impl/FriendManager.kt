package dev.kura.net.manager.impl

import net.minecraft.entity.player.PlayerEntity
import java.util.concurrent.CopyOnWriteArrayList

object FriendManager {

    fun init() {

    }

    val friends = CopyOnWriteArrayList<String>()

    fun isFriend(name: String): Boolean = name in friends

    fun isFriend(player: PlayerEntity): Boolean = isFriend(player.name.string)

}