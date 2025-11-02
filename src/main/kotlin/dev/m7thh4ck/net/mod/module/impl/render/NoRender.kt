package dev.m7thh4ck.net.mod.module.impl.render

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.ParticleEvent
import dev.kura.net.event.impl.UpdateWalkingEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.client.particle.ExplosionLargeParticle
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity

object NoRender: Module("NoRender", Category.Render) {

    private val xp by setting("XP", true)
    val noHurtCam by setting("NoHurtCam",true)
    val fireOverlay by setting("FireOverlay", true)
    private val explosions by setting("Explosions", true)
    val fog by setting("Fog", true)
    val darkness by setting("Darkness", true)
    val blindness by setting("Blindness", true)

    init {

        eventListener<UpdateWalkingEvent.Post> {
            if (fullNullCheck()) return@eventListener

            world.entities.forEach {
                when (it) {
                    is ExperienceBottleEntity -> {
                        if (xp) {
                            world.removeEntity(it.id, Entity.RemovalReason.KILLED)
                        }
                    }
                }
            }
        }

        eventListener<ParticleEvent.AddParticle> {
            if (explosions && it.particle is ExplosionLargeParticle) it.cancel()
        }

    }

}