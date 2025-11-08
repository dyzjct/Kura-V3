package dev.m7thh4ck.net.managers

import dev.m7thh4ck.net.managers.impl.*

object Managers {

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