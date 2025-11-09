package dev.kura.net.utils.animation

import dev.kura.net.utils.graphics.easing.Easing
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class SmoothBoxRenderer(
    private val easing: Easing = Easing.OUT_QUART,
    private var durationMs: Long = 400L
) {
    private var prevPos: Vec3d? = null
    private var currentPos: Vec3d? = null
    private var lastUpdateTime: Long = 0L
    private var fadeTime: Long = 0L

    /**
     * 更新目标位置，启动插值动画
     */
    fun updateTarget(pos: Vec3d) {
        if (currentPos != pos) {
            prevPos = currentPos ?: pos
            currentPos = pos
            lastUpdateTime = System.currentTimeMillis()
        }
        fadeTime = System.currentTimeMillis()
    }

    /**
     * 设置插值动画的时长（单位：毫秒）Ï
     */
    fun setDuration(durationMs: Long) {
        this.durationMs = durationMs
    }

    /**
     * 重置状态
     */
    fun reset() {
        prevPos = null
        currentPos = null
    }

    /**
     * 获取插值后的 Box，用于渲染
     */
    fun getBox(motionEnabled: Boolean = true): Box? {
        val from = prevPos ?: return null
        val to = currentPos ?: return null
        val now = System.currentTimeMillis()

        val delta = (now - lastUpdateTime).coerceIn(0L, durationMs).toFloat() / durationMs
        val t = easing.inc(delta)

        val interp = if (motionEnabled) from.lerp(to, t.toDouble()) else to

        val halfSize = 0.5
        return Box(
            interp.x - halfSize,
            interp.y - halfSize,
            interp.z - halfSize,
            interp.x + halfSize,
            interp.y + halfSize,
            interp.z + halfSize
        )
    }

    fun getLastUpdateTime(): Long {
        return fadeTime
    }

    /**
     * 获取插值后的位置 Vec3d（例如用于文本渲染）
     */
    fun getInterpolatedPos(motionEnabled: Boolean = true): Vec3d? {
        val from = prevPos ?: return null
        val to = currentPos ?: return null
        val now = System.currentTimeMillis()

        val delta = (now - lastUpdateTime).coerceIn(0L, durationMs).toFloat() / durationMs
        val t = easing.inc(delta)

        return if (motionEnabled) from.lerp(to, t.toDouble()) else to
    }
}