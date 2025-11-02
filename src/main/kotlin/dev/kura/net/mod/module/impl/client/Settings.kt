package dev.kura.net.mod.module.impl.client

import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module

object Settings:Module(name = "Settings",Category.Client) {
    val swingHand by setting("SwingHand",SwingMode.MainHand)

    enum class SwingMode{
        MainHand,OFFHand,Packet,None
    }
}