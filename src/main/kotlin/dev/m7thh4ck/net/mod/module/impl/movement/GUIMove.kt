package dev.m7thh4ck.net.mod.module.impl.movement

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.util.other.ScreenUtils.notWhiteListScreen
import net.minecraft.client.util.InputUtil

object GUIMove : Module(name = "GUIMove", category = Category.Movement) {
    init {
        eventListener<TickEvent.Post> {
            if (fullNullCheck()) return@eventListener
            mc.currentScreen?.let {
                if (it.notWhiteListScreen()) {
                    return@eventListener
                }
            }
            for (k in arrayOf(
                mc.options.forwardKey,
                mc.options.backKey,
                mc.options.leftKey,
                mc.options.rightKey,
                mc.options.jumpKey,
                mc.options.sprintKey
            )) {
                k.isPressed = InputUtil.isKeyPressed(
                    mc.window.handle,
                    InputUtil.fromTranslationKey(k.boundKeyTranslationKey).code
                )
            }
        }
    }
}