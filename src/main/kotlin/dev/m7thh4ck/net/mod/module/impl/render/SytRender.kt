package dev.m7thh4ck.net.mod.module.impl.render

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.Render2DEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.graphics.Render2DEngine
import java.awt.Color

object SytRender: Module("SytRender", Category.Render) {

    private val Red by setting("Red", 255, 0..255, 1)
    private val Green by setting("Green", 32, 0..255, 1)
    private val Blue by setting("Blue", 32, 0..255, 1)
    private val alpha by setting("Alpha", 70, 0..255, 1)

    init {

        eventListener<Render2DEvent> { event ->
            if (fullNullCheck()) return@eventListener
            val factor: Float = 1f - clamp(player.health, 0f, 12f) / 12f
            val red = Color(Red, Green, Blue,alpha)

            if (factor < 1f)
                Render2DEngine.draw2DGradientRect(
                    event.context.matrices, 0.0f, 0.0f,
                    mc.window.scaledWidth.toFloat(), mc.window.scaledHeight.toFloat(),
                    Render2DEngine.injectAlpha(red, (factor * 170f).toInt()), red,
                    Render2DEngine.injectAlpha(red, (factor * 170f).toInt()), red
                )
        }
    }


    fun clamp(num: Float, min: Float, max: Float): Float {
        return if (num < min) min else Math.min(num, max)
    }



}