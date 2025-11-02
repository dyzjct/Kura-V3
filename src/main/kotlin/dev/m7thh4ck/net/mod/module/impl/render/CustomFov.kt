package dev.m7thh4ck.net.mod.module.impl.render

import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module

object CustomFov : Module(
    name = "CustomFov",
    category = Category.Render,
) {
    var fov by setting("Fov", 120.0, 0.0..160.0, 1.0)
    var itemFov by setting("ItemFov", false)
    var itemFovModifier by setting("ItemModifier", 120.0, 0.0..358.0, 1.0){ itemFov }
}