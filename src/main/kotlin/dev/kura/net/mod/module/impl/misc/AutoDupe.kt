package dev.kura.net.mod.module.impl.misc

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.kura.net.utils.interfaces.DisplayEnum
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.kura.net.mod.module.impl.misc.autodupe.Xin231125Dupe

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