package asm.kura.mixins;

import dev.kura.net.event.impl.RenderLivingLabelEvent;
import dev.kura.net.mod.module.impl.render.FullBright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderLabelIfPresent(T entity, Text text, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            RenderLivingLabelEvent event = new RenderLivingLabelEvent(entity);
            event.post();
            if (event.isCancelled()) ci.cancel();
        }
    }

    @Inject(method = "getSkyLight", at = @At("RETURN"), cancellable = true)
    private void onGetSkyLight(CallbackInfoReturnable<Integer> cir) {
        if (FullBright.INSTANCE.isEnabled())
            cir.setReturnValue(FullBright.INSTANCE.getBrightness());
    }

}
