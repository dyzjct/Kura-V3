package dev.kura.net.event.impl

import dev.kura.net.event.eventListener
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.client.ClickGUI
import dev.m7thh4ck.net.mod.module.impl.client.Debug
import dev.m7thh4ck.net.mod.module.impl.client.HUD
import dev.m7thh4ck.net.mod.module.impl.client.Settings
import dev.m7thh4ck.net.mod.module.impl.combat.AnchorAura
import dev.m7thh4ck.net.mod.module.impl.combat.AutoTotem
import dev.m7thh4ck.net.mod.module.impl.combat.AutoWeb
import dev.m7thh4ck.net.mod.module.impl.combat.BowBomb
import dev.m7thh4ck.net.mod.module.impl.combat.CityMiner
import dev.m7thh4ck.net.mod.module.impl.combat.Criticals
import dev.m7thh4ck.net.mod.module.impl.combat.HolePush
import dev.m7thh4ck.net.mod.module.impl.combat.KillAura
import dev.m7thh4ck.net.mod.module.impl.combat.M7thAura
import dev.m7thh4ck.net.mod.module.impl.combat.Quiver
import dev.m7thh4ck.net.mod.module.impl.combat.SelfFill
import dev.m7thh4ck.net.mod.module.impl.combat.Surround
import dev.m7thh4ck.net.mod.module.impl.combat.TotemPopCounter
import dev.m7thh4ck.net.mod.module.impl.misc.AutoDupe
import dev.m7thh4ck.net.mod.module.impl.misc.BaseFinder
import dev.m7thh4ck.net.mod.module.impl.misc.ChatSuffix
import dev.m7thh4ck.net.mod.module.impl.misc.FakePlayer
import dev.m7thh4ck.net.mod.module.impl.misc.MCP
import dev.m7thh4ck.net.mod.module.impl.movement.AntiWeb
import dev.m7thh4ck.net.mod.module.impl.movement.BlockClip
import dev.m7thh4ck.net.mod.module.impl.movement.Flight
import dev.m7thh4ck.net.mod.module.impl.movement.GUIMove
import dev.m7thh4ck.net.mod.module.impl.movement.NoSlow
import dev.m7thh4ck.net.mod.module.impl.movement.Speed
import dev.m7thh4ck.net.mod.module.impl.movement.Sprint
import dev.m7thh4ck.net.mod.module.impl.movement.Step
import dev.m7thh4ck.net.mod.module.impl.movement.Strafe
import dev.m7thh4ck.net.mod.module.impl.movement.Velocity
import dev.m7thh4ck.net.mod.module.impl.player.AutoMend
import dev.m7thh4ck.net.mod.module.impl.player.AutoReplenish
import dev.m7thh4ck.net.mod.module.impl.player.HitboxDesync
import dev.m7thh4ck.net.mod.module.impl.player.NoFall
import dev.m7thh4ck.net.mod.module.impl.player.NoRotate
import dev.m7thh4ck.net.mod.module.impl.player.PacketEat
import dev.m7thh4ck.net.mod.module.impl.player.PacketMine
import dev.m7thh4ck.net.mod.module.impl.render.Animation
import dev.m7thh4ck.net.mod.module.impl.render.AspectRatio
import dev.m7thh4ck.net.mod.module.impl.render.BlockHighLight
import dev.m7thh4ck.net.mod.module.impl.render.CustomFov
import dev.m7thh4ck.net.mod.module.impl.render.FullBright
import dev.m7thh4ck.net.mod.module.impl.render.HoleESP
import dev.m7thh4ck.net.mod.module.impl.render.NameTags
import dev.m7thh4ck.net.mod.module.impl.render.NoRender
import dev.m7thh4ck.net.mod.module.impl.render.Notification
import dev.m7thh4ck.net.mod.module.impl.render.PlaceRender
import dev.m7thh4ck.net.mod.module.impl.render.SytRender
import dev.m7thh4ck.net.mod.module.impl.render.ViewModel
import dev.m7thh4ck.net.util.Wrapper

object ModuleManager {

    private val modules: MutableList<Module> = arrayListOf()
    var sortedModules: List<Module> = arrayListOf()

    init {

        eventListener<KeyBoardEvent>(Int.MAX_VALUE, true) {
            if (Wrapper.mc.currentScreen != null) return@eventListener

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