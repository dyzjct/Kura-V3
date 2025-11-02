package dev.kura.net.utils.player

import dev.kura.net.manager.impl.FriendManager
import dev.kura.net.utils.Wrapper.player
import dev.kura.net.utils.Wrapper.world
import dev.kura.net.utils.entity.EntityUtil.eyePosition
import dev.kura.net.utils.entity.EntityUtil.scaledHealth
import dev.kura.net.utils.extension.sq
import dev.kura.net.utils.math.vector.distanceSqTo
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity

fun getTarget(range: Double): PlayerEntity? {
    for (ent in world.entities.filter {
        player.distanceSqTo(it.blockPos) <= range.sq && it.isAlive && it is PlayerEntity && it != player && !FriendManager.isFriend(
            it
        )
    }.sortedBy { player.distanceSqTo(it.blockPos) }) {
        if (ent !is PlayerEntity) continue
        return ent
    }
    return null
}

fun getTargetsToList(range: Float): List<PlayerEntity> {
    val rangeSq = range.sq
    val list = ObjectArrayList<PlayerEntity>()
    val eyePos = player.eyePosition

    for (target in world.entities.filter { it is PlayerEntity && it.isAlive && it.distanceSqTo(eyePos) <= rangeSq }) {
        if (FriendManager.isFriend(target.entityName)) continue
        if (target !is PlayerEntity) continue
        list.add(target)
    }

    return list.filter { it.isAlive }
        .sortedBy { (it as LivingEntity).scaledHealth }
        .toList()
}