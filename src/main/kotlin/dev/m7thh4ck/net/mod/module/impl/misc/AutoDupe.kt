package dev.m7thh4ck.net.mod.module.impl.misc

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.misc.autodupe.Xin231125Dupe

object AutoDupe: Module("AutoDupe", Category.Misc) {

    private val mode by setting("Mode", Mode.Xin231125)

    init {

        eventListener<TickEvent> {

            if (fullNullCheck()) return@eventListener

            when (mode) {
                Mode.Xin231125 -> Xin231125Dupe.onTick()
            }

        }

    }

    private enum class Mode(override val displayName: CharSequence): DisplayEnum {
        Xin231125("Xin231125")
    }

}