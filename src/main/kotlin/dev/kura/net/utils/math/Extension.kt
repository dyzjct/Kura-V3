package dev.kura.net.utils.math

import kotlin.math.PI

const val PI_FLOAT = 3.141593f

fun Float.toRadian() = this / 180.0f * PI_FLOAT

fun Double.toRadian() = this / 180.0 * PI

fun Float.toDegree() = this * 180.0f / PI_FLOAT

fun Double.toDegree() = this * 180.0 / PI
