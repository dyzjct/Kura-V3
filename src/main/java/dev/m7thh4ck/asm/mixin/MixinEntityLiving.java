package dev.m7thh4ck.asm.mixin;


import dev.m7thh4ck.net.mod.module.impl.render.Animation;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public class MixinEntityLiving{
    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getArmSwingAnimationEnd(final CallbackInfoReturnable<Integer> info) {
        if (Animation.INSTANCE.isEnabled() && Animation.INSTANCE.getSlowAnimation()) {
            info.setReturnValue(Animation.INSTANCE.getSlowAnimationVal());
        }
    }
}
