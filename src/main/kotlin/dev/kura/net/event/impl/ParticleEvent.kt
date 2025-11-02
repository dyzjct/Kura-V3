package dev.kura.net.event.impl

import dev.kura.net.event.CancellableEvent
import net.minecraft.client.particle.Particle
import net.minecraft.particle.ParticleEffect

sealed class ParticleEvent {

    class AddParticle(val particle: Particle): CancellableEvent()

    class AddEmmiter(val emmiter: ParticleEffect): CancellableEvent()

}