package dev.kura.net.mod.module.impl.render

import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module

object FullBright : Module("FullBright", Category.Render) {

    val brightness by setting("Brightness", 15, 0..15, 1)

}