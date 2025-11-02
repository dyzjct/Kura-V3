package dev.m7thh4ck.net.settings

abstract class AbstractNumberSettings<N: Number>(
    displayName: CharSequence,
    value: N,
    val minValue: N,
    val maxValue: N,
    val step: N,
    visibility: () -> Boolean
): AbstractSetting<N>(displayName, value, visibility)

class IntSetting(
    displayName: CharSequence,
    value: Int,
    range: IntRange,
    step: Int,
    visibility: () -> Boolean = { true }
) : AbstractNumberSettings<Int>(displayName, value, range.first, range.last, step, visibility)

class FloatSetting(
    displayName: CharSequence,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    visibility: () -> Boolean = { true },
) : AbstractNumberSettings<Float>(displayName, value, range.start, range.endInclusive, step, visibility)

class DoubleSetting(
    displayName: CharSequence,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    step: Double,
    visibility: () -> Boolean = { true },
) : AbstractNumberSettings<Double>(displayName, value, range.start, range.endInclusive, step, visibility)