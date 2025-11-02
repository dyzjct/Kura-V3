package dev.kura.net.manager

import dev.kura.net.manager.impl.CombatManager
import dev.kura.net.manager.impl.CommandManager
import dev.kura.net.manager.impl.ConfigManager
import dev.kura.net.manager.impl.FriendManager
import dev.kura.net.event.impl.ModuleManager
import dev.kura.net.manager.impl.RotationManager

object Manager {

    fun init() {
        ModuleManager.init()
        CommandManager.init()
        FriendManager.init()
        ConfigManager.init()
        CombatManager.init()
        RotationManager.init()
    }

    fun unload() {
        ConfigManager.unload()
    }

}