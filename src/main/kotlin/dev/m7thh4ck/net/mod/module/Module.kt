package dev.m7thh4ck.net.mod.module

import dev.kura.net.event.EventBus
import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.GameLoopEvent
import dev.kura.net.event.impl.KeyBoardEvent
import dev.kura.net.event.impl.PlayerMotionEvent
import dev.m7thh4ck.net.mod.module.impl.render.Notification
import dev.m7thh4ck.net.settings.AbstractSetting
import dev.m7thh4ck.net.settings.KeyBindSetting
import dev.m7thh4ck.net.settings.SettingDesigner
import dev.m7thh4ck.net.util.helper.ChatUtil
import dev.m7thh4ck.net.util.keyboard.Bind
import dev.m7thh4ck.net.util.player.PlayerUtil.swingHand
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Formatting
import java.util.concurrent.CopyOnWriteArrayList

abstract class Module(
    var name: String,
    val category: Category,
    val hudInfo: Boolean = false
) : SettingDesigner<Module> {

    private var isEnable = false

    private val settings: ArrayList<AbstractSetting<*>> = arrayListOf()
    private val enableCustomers = CopyOnWriteArrayList<() -> Unit>()
    private val disableCustomers = CopyOnWriteArrayList<() -> Unit>()
    private val motionCustomers = CopyOnWriteArrayList<() -> Unit>()
    private val gameTickCustomers = CopyOnWriteArrayList<() -> Unit>()

    protected val mc: MinecraftClient get() = MinecraftClient.getInstance()!!
    protected val player: ClientPlayerEntity get() = mc.player!!
    protected val connection: ClientPlayNetworkHandler get() = mc.player!!.networkHandler
    protected val world: ClientWorld get() = mc.world!!

    val keybind by setting("Bind", Bind(-1), {})
    val drawn by setting("Drawn", true)

    init {
        eventListener<KeyBoardEvent> {
            for (set in settings) {
                if (set is KeyBindSetting && set.value.key == it.key) {
                    set.func.invoke()
                }
            }
        }

        eventListener<PlayerMotionEvent> {
            motionCustomers.forEach { it.invoke() }
        }

        eventListener<GameLoopEvent.Tick> {
            gameTickCustomers.forEach { it.invoke() }
        }
    }

    fun swing() {
        swingHand()
    }

    fun setEnable(enable: Boolean) {
        isEnable = enable
        if (enable) {
            EventBus.subscribe(this)
            enableCustomers.forEach { it.invoke() }
            if (Notification.isEnabled()) ChatUtil.sendRawMessage(name + Formatting.GREEN + " Enabled")
        } else {
            EventBus.unsubscribe(this)
            disableCustomers.forEach { it.invoke() }
            if (Notification.isEnabled()) ChatUtil.sendRawMessage(name + Formatting.RED + " Disabled")
        }
    }

    fun setEnableSilent(enable: Boolean) {
        isEnable = enable

        if (enable) {
            EventBus.subscribe(this)
            enableCustomers.forEach { it.invoke() }
        } else {
            EventBus.unsubscribe(this)
            disableCustomers.forEach { it.invoke() }
        }
    }

    fun onMotion(func: () -> Unit) = motionCustomers.add { func }
    fun onGameTick(func: () -> Unit) = gameTickCustomers.add { func }
    fun onEnable(func: () -> Unit) = enableCustomers.add(func)
    fun onDisable(func: () -> Unit) = disableCustomers.add(func)

    open fun getHudInfo(): String = ""

    fun enable() {
        setEnable(true)
    }

    fun disable() {
        setEnable(false)
    }

    fun toggle() {
        setEnable(!isEnabled())
    }

    fun isEnabled(): Boolean {
        return isEnable
    }

    override fun <S : AbstractSetting<*>> Module.setting(setting: S): S {
        settings.add(setting)
        return setting
    }

    fun getSettings(): ArrayList<AbstractSetting<*>> {
        return settings
    }

    protected fun fullNullCheck(): Boolean = mc.player == null || mc.world == null
}
