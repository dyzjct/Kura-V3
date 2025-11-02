package dev.m7thh4ck.asm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.m7thh4ck.net.M7thH4ck;
import dev.m7thh4ck.net.event.impl.Render2DEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            client.getProfiler().push(M7thH4ck.MODID + "_render_2d");
            Render2DEvent event = new Render2DEvent(context);
            event.post();
            RenderSystem.applyModelViewMatrix();
            client.getProfiler().pop();
        }
    }
}
