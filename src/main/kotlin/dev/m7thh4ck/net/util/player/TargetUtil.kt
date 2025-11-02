package dev.m7thh4ck.net.util.player

import dev.m7thh4ck.net.managers.impl.FriendManager
import dev.m7thh4ck.net.util.Wrapper.player
import dev.m7thh4ck.net.util.Wrapper.world
import dev.m7thh4ck.net.util.entity.EntityUtil.eyePosition
import dev.m7thh4ck.net.util.entity.EntityUtil.scaledHealth
import dev.m7thh4ck.net.util.extension.sq
import dev.m7thh4ck.net.util.math.vector.distanceSqTo
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