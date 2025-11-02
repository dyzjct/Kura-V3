package dev.m7thh4ck.net.settings

import dev.m7thh4ck.net.util.keyboard.Bind

interface SettingDesigner<T : Any> {
    fun T.setting(
        displayName: CharSequence,
        value: Boolean,
        visibility: () -> Boolean = { true },
    ) = setting(BooleanSetting(displayName, value, visibility))

    fun T.setting(
        displayName: CharSequence,
        value: Int,
        range: IntRange,
        step: Int,
        visibility: () -> Boolean = { true },
    ) = setting(IntSetting(displayName, value, range, step, visibility))

    fun T.setting(
        displayName: CharSequence,
        value: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        visibility: () -> Boolean = { true },
    ) = setting(FloatSetting(displayName, value, range, step, visibility))

    fun T.setting(
        displayName: CharSequence,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        visibility: () -> Boolean = { true },
    ) = setting(DoubleSetting(displayName, value, range, step, visibility))

    fun T.setting(
        displayName: CharSequence,
        value: String,
        visibility: () -> Boolean = { true },
    ) = setting(StringSetting(displayName, value, visibility))

    fun T.setting(
        displayName: CharSequence,
        value: Bind,
        func: () -> Unit,
        visibility: () -> Boolean = { true },
    ) = setting(KeyBindSetting(displayName, value, func, visibility))

    fun <E> T.setting(
        displayName: CharSequence,
        value: E,
        visibility: () -> Boolean = { true },
    ) where E : Enum<E> = setting(EnumSetting(displayName, value, visibility))

    fun <S : AbstractSetting<*>> T.setting(setting: S): S
}