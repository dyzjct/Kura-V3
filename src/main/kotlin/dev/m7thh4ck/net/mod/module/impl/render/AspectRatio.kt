package dev.m7thh4ck.net.mod.module.impl.render

import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module

object AspectRatio : Module("AspectRatio", Category.Render) {


    val nohurtcam by setting("ratio", 1.78f, 1.0f..5.0f, 0.1f)

}