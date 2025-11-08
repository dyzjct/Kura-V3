package dev.m7thh4ck.net.util.graphics


fun interface InterpolateFunction {
    operator fun invoke(time: Long, prev: Float, current: Float): Float
}