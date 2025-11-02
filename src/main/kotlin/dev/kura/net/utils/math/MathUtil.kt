package dev.kura.net.utils.math

import dev.kura.net.utils.Util
import kotlin.math.min


object MathUtil: Util() {
    fun clamp(num: Float, min: Float, max: Float): Float {
        return if (num < min) min else min(num.toDouble(), max.toDouble()).toFloat()
    }
}
