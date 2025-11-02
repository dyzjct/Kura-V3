package dev.kura.net.utils.entity

import dev.kura.net.utils.Wrapper.mc
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import kotlin.math.hypot

val currentTps: Float = mc.renderTickCounter.tickTime / 1000.0f

fun getTargetSpeed(target: PlayerEntity): Double {
    return hypot((target.x - target.prevX), (target.z - target.prevZ)) / currentTps * 3.6 //用LagCompensator会有问题
}

fun getTargetSpeed(target: LivingEntity): Double {
    return hypot((target.x - target.prevX), (target.z - target.prevZ)) / currentTps * 3.6
}

fun getTargetSpeed(target: Entity): Double {
    return hypot((target.x - target.prevX), (target.z - target.prevZ)) / currentTps * 3.6
}