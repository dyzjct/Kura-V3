package dev.m7thh4ck.net.util.math

import dev.m7thh4ck.net.util.Util
import kotlin.math.min


object MathUtil: Util() {
    fun clamp(num: Float, min: Float, max: Float): Float {
        return if (num < min) min else min(num.toDouble(), max.toDouble()).toFloat()
    }
}
