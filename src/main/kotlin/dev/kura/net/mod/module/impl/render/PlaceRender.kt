package dev.kura.net.mod.module.impl.render

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.Render3DEvent
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.kura.net.utils.entity.scale
import dev.kura.net.utils.graphics.ESPRenderer
import dev.kura.net.utils.graphics.easing.Easing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object PlaceRender : Module(
    name = "PlaceRender",
    category = Category.Render
) {
    private val mode by setting("Mode", Mode.Fade)
    private val fadeLength by setting("FadeLength", 200, 0..1000, 1)
    private val red by setting("Red", 255, 0..255, 1)
    private val green by setting("Red", 255, 0..255, 1)
    private val blue by setting("Red", 255, 0..255, 1)
    private val file by setting("FileAlpha", 80, 0..255, 1)
    private val line by setting("LineAlpha", 255, 0..255, 1)
    val renderBlocks = ConcurrentHashMap<BlockPos, Long>()

    init {
        eventListener<Render3DEvent> {
            runCatching {
                renderBlocks.forEach { (pos: BlockPos, time: Long) ->
                    if (System.currentTimeMillis() - time > fadeLength) {
                        renderBlocks.remove(pos)
                    } else {
                        val scale = Easing.IN_CUBIC.dec(Easing.toDelta(time, fadeLength))
                        val renderer = ESPRenderer()
                        var box = Box(pos)
                        box = when (mode) {
                            Mode.Fade -> box.scale(1.0)
                            Mode.Glide -> box.scale(scale.toDouble())
                        }
                        renderer.aFilled = (file * scale).toInt()
                        renderer.aOutline = (line * scale).toInt()
                        renderer.add(
                            box, Color(red, green, blue)
                        )
                        renderer.render(it.matrices, false)
                    }
                }

            }
        }
    }

    enum class Mode {
        Fade, Glide
    }
}