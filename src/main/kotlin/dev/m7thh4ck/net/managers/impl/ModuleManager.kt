package dev.m7thh4ck.net.managers.impl

import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.KeyBoardEvent
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.client.ClickGUI
import dev.m7thh4ck.net.mod.module.impl.client.Debug
import dev.m7thh4ck.net.mod.module.impl.client.HUD
import dev.m7thh4ck.net.mod.module.impl.client.Settings
import dev.m7thh4ck.net.mod.module.impl.combat.*
import dev.m7thh4ck.net.mod.module.impl.misc.*
import dev.m7thh4ck.net.mod.module.impl.movement.*
import dev.m7thh4ck.net.mod.module.impl.player.*
import dev.m7thh4ck.net.mod.module.impl.render.*


object ModuleManager {

    private val modules: MutableList<Module> = arrayListOf()
    var sortedModules: List<Module> = arrayListOf()

    init {

        eventListener<KeyBoardEvent>(Int.MAX_VALUE, true) {
            if (dev.m7thh4ck.net.util.Wrapper.mc.currentScreen != null) return@eventListener

            for (mod in modules) {
                if (mod.keybind.key == it.key) {
                    mod.toggle()
                }
            }
        }

    }

    fun init() {
        regModules()
    }

    private fun regModules() {
        // Client
        modules.add(ClickGUI)
        modules.add(HUD)
        modules.add(Debug)
        modules.add(Settings)

        // Combat
        modules.add(AnchorAura)
        modules.add(M7thAura)
        modules.add(AutoTotem)
        modules.add(BowBomb)
        modules.add(TotemPopCounter)
        modules.add(SelfFill)
        modules.add(Criticals)
        modules.add(KillAura)
        modules.add(Quiver)
        modules.add(CityMiner)
        modules.add(HolePush)
        modules.add(Surround)

        // Player
        modules.add(NoRotate)
        modules.add(PacketEat)
        modules.add(PacketMine)
        modules.add(NoFall)
        modules.add(HitboxDesync)
        modules.add(AutoMend)
        modules.add(AutoReplenish)

        // Movement
        modules.add(Sprint)
        modules.add(AntiWeb)
        modules.add(Step)
        modules.add(Velocity)
        modules.add(NoSlow)
        modules.add(BlockClip)
        modules.add(Flight)
        modules.add(GUIMove)
        modules.add(Strafe)
        modules.add(Speed)
        modules.add(AutoWeb)

        // Render
        modules.add(Notification)
        modules.add(FullBright)
        modules.add(BlockHighLight)
        modules.add(AspectRatio)
        modules.add(Animation)
        modules.add(ViewModel)
        modules.add(HoleESP)
        modules.add(NameTags)
        modules.add(NoRender)
        modules.add(SytRender)
        modules.add(PlaceRender)
        modules.add(CustomFov)

        // Misc
        modules.add(FakePlayer)
        modules.add(BaseFinder)
        modules.add(ChatSuffix)
        modules.add(AutoDupe)
        modules.add(MCP)
    }

    fun getModules(): List<Module> {
        return modules
    }

    fun getEnabledModules(): List<Module> {
        val list: MutableList<Module> = mutableListOf()
        for (mod in modules) {
            if (mod.isEnabled()) list.add(mod)
        }
        return list
    }

    fun sortModules() {
        sortedModules = modules.sortedBy { it.name }
    }

    fun getByName(name: String): Module? {
        for (mod in modules) {
            if (name.equals(mod.name, ignoreCase = true)) {
                return mod
            }
        }
        return null
    }


}