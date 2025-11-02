package dev.m7thh4ck.net.mod.module.impl.player

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.interfaces.DisplayEnum
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.player.InventoryUtil
import dev.m7thh4ck.net.util.player.PlayerUtil
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand


object AutoMend : Module("AutoMend", Category.Player) {
    private val MendMod by setting("AutoMendMod", Instant.spoof)
    private val Delay by setting("Delay", 100, 0..500, 1)
    private val eatingPause by setting("EatingPause", false)
    private val swing by setting("Swing", false)
    private val PlayerRot by setting("EatingRotation", true)
    private val Auto by setting("AutoToggle", true)
    private val DDelay by setting("ToggleDelay", 8000, 0..10000, 1) {Auto}
    private val timer = TimerUtils()
    private val InvTimer = TimerUtils()
    init {
        eventListener<TickEvent.Post> {

            if (player.isUsingItem && eatingPause) return@eventListener
            val xp = InventoryUtil.findItemInHotbar(Items.EXPERIENCE_BOTTLE)
            if (xp == null) {
                disable()
                return@eventListener
            }

            RotationManager.addRotation(
                player.yaw, 86.0F,
                0
            )
            if (PlayerRot) {
                RotationManager.stopRotation()
                sendPlayerRot(player.yaw, 86f, false)
                RotationManager.startRotation()
            }
            when (MendMod) {
                Instant.Bypass -> {
                    InventoryUtil.spoofHotbarBypass(xp ?: -1) {
                        if (timer.tickAndReset(Delay)) {
                            if (swing) {
                                swing()
                            }
                            player.networkHandler.sendPacket(
                                PlayerInteractItemC2SPacket(
                                    Hand.MAIN_HAND,
                                    PlayerUtil.getWorldActionId(world)
                                )
                            )
                            timer.reset()
                        }
                    }
                }
                Instant.spoof -> {
                    InventoryUtil.spoofHotbar(xp ?: -1) {
                        if (timer.tickAndReset(Delay)) {
                            if (swing) {
                                swing()
                            }
                            player.networkHandler.sendPacket(
                                PlayerInteractItemC2SPacket(
                                    Hand.MAIN_HAND,
                                    PlayerUtil.getWorldActionId(world)
                                )
                            )
                            timer.reset()
                        }
                    }
                }
            }
            if(Auto) {
                if (InvTimer.tickAndReset(DDelay)) {
                    disable()
                    InvTimer.reset()
                }
            }
            }
    }
    private fun sendPlayerRot(yaw: Float, pitch: Float, onGround: Boolean) {
        player.networkHandler.sendPacket(PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround))
    }

    private enum class Instant(override val displayName: CharSequence) : DisplayEnum {
        spoof("spoof"),
        Bypass("Bypass"),

    }
}



