package dev.m7thh4ck.net.mod.module.impl.render

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.Render3DEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.graphics.ESPRenderer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult

import java.awt.Color


object BlockHighLight : Module("BlockHighLight", Category.Render) {
    private val red by setting("Red", 255, 0..255, 1)
    private val green by setting("Red", 255, 0..255, 1)
    private val blue by setting("Red", 255, 0..255, 1)
    private val file by setting("FileAlpha", 80, 0..255, 1)
    private val line by setting("LineAlpha", 255, 0..255, 1)


    init {
        eventListener<Render3DEvent> {
            if (mc.crosshairTarget!!.type !== HitResult.Type.BLOCK) return@eventListener
            if (mc.crosshairTarget !is BlockHitResult) {
                return@eventListener
            }
                        val renderer = ESPRenderer()
                        renderer.aFilled = (file )
                        renderer.aOutline = (line )
                        renderer.add(
                            ((mc.crosshairTarget as BlockHitResult).blockPos), Color(red, green, blue)
                        )
            renderer.render(it.matrices, false)

            }
        }
    }




