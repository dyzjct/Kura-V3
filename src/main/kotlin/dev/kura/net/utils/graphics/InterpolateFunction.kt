package dev.kura.net.utils.graphics


fun interface InterpolateFunction {
    operator fun invoke(time: Long, prev: Float, current: Float): Float
}