package dev.m7thh4ck.asm.mixin;

import dev.m7thh4ck.net.KURA;
import dev.m7thh4ck.net.event.impl.GameLoopEvent;
import dev.m7thh4ck.net.event.impl.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "run", at = @At("HEAD"))
    public void run(CallbackInfo ci) {
        KURA.load();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo ci) {
        KURA.unload();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo info) {
        TickEvent.Pre.INSTANCE.post();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo info) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            TickEvent.Post.INSTANCE.post();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter;beginRenderTick(J)I", shift = At.Shift.BEFORE))
    public void render$Inject$INVOKE$updateTimer(boolean tick, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            GameLoopEvent.Start.INSTANCE.post();
        }

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0, shift = At.Shift.AFTER))
    public void renderTick(boolean tick, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            GameLoopEvent.Tick.INSTANCE.post();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;updateMouse()V", shift = At.Shift.BEFORE))
    public void render$Inject$INVOKE$endStartSection(boolean tick, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            GameLoopEvent.Render.INSTANCE.post();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 7, shift = At.Shift.BEFORE))
    public void render$Inject$INVOKE$isFramerateLimitBelowMax(boolean tick, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            GameLoopEvent.End.INSTANCE.post();
        }
    }


}
