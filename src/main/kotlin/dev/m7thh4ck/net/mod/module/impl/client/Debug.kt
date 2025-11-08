package dev.m7thh4ck.net.mod.module.impl.client

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.Render2DEvent
import dev.m7thh4ck.net.util.graphics.TextUtil
import dev.m7thh4ck.net.util.graphics.color.ColorRGB
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module

object Debug: Module("Debug", Category.Client) {

    private val scale by setting("Scale", 2.0f, 0.0f..3.0f, 0.1f)

    init {

        eventListener<Render2DEvent> {

            TextUtil.drawStringWithScale(it.context, "原神", 25f, 25f, ColorRGB(255, 255, 255), true, scale)

        }

    }

}