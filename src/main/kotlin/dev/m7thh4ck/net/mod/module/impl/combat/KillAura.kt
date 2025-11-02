package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.TickEvent
import dev.m7thh4ck.net.managers.impl.RotationManager
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.util.Wrapper.playerController
import dev.m7thh4ck.net.util.entity.getTargetSpeed
import dev.m7thh4ck.net.util.math.TimerUtils
import dev.m7thh4ck.net.util.player.getTarget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.SwordItem

object KillAura : Module(
    name = "KillAura",
    category = Category.Combat,
)  {

    private var range by setting("Range", 4.5, 0.1..6.0 ,0.1)
    private var onlySword by setting("OnlySword", true)
    private var delay by setting("Delay", 1108, 0..2000, 1)
    private val Timer = TimerUtils()
    var target: PlayerEntity? = null
    override fun getHudInfo(): String {
        target?.let {
            return "${it.name.string} ${getTargetSpeed(it) > 20.0}"
        } ?: return "QAQ"
    }

    init {
        eventListener<TickEvent.Post> {

            target = getTarget(range)
            target?.let { target ->
                if (onlySword && player.mainHandStack.item !is SwordItem) return@eventListener
                RotationManager.addRotation(target.blockPos.up())
                if (Timer.tickAndReset(delay)) {
                    playerController.attackEntity(player, target)
                    swing()
                    Timer.reset()
                }
            }

        }

    }
}


