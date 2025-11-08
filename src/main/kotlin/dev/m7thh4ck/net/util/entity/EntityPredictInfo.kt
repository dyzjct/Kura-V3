package dev.m7thh4ck.net.util.entity

import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

data class EntityPredictInfo(val entity: LivingEntity, val pos: Vec3d, val box: Box, val currentPos: Vec3d, val predictMotion: Vec3d)