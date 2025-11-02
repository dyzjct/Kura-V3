package asm.kura.mixins;

import dev.kura.net.event.impl.ParticleEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
    public void onAddParticle(Particle particle, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            ParticleEvent.AddParticle event = new ParticleEvent.AddParticle(particle);
            event.post();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", cancellable = true)
    public void onAddEmmiter(Entity entity, ParticleEffect particleEffect, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            ParticleEvent.AddEmmiter event = new ParticleEvent.AddEmmiter(particleEffect);
            event.post();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", cancellable = true)
    public void onAddEmmiterAged(Entity entity, ParticleEffect particleEffect, int maxAge, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            ParticleEvent.AddEmmiter event = new ParticleEvent.AddEmmiter(particleEffect);
            event.post();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

}
