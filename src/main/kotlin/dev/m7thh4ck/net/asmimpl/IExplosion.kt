package dev.m7thh4ck.net.asmimpl

import net.minecraft.util.math.Vec3d

interface IExplosion {
    operator fun set(pos: Vec3d?, power: Float, createFire: Boolean)
}