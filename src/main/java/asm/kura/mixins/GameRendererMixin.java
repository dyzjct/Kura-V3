package asm.kura.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kura.net.event.impl.Render3DEvent;
import dev.m7thh4ck.net.mod.module.impl.render.AspectRatio;
import dev.m7thh4ck.net.mod.module.impl.render.CustomFov;
import dev.m7thh4ck.net.mod.module.impl.render.NoRender;
import dev.m7thh4ck.net.util.graphics.Render3DEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;

    @Shadow
    private float zoomY;

    @Shadow
    private float viewDistance;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
            opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void render3dHook(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            Render3DEngine.INSTANCE.getLastProjMat().set(RenderSystem.getProjectionMatrix());
            Render3DEngine.INSTANCE.getLastModMat().set(RenderSystem.getModelViewMatrix());
            Render3DEngine.INSTANCE.getLastWorldSpaceMatrix().set(matrix.peek().getPositionMatrix());

            Render3DEvent event = new Render3DEvent(matrix, tickDelta);
            event.post();
        }
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (AspectRatio.INSTANCE.isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * 0.01745329238474369), AspectRatio.INSTANCE.getNohurtcam(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getNoHurtCam()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", cancellable = true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cb) {
        if (CustomFov.INSTANCE.isEnabled()) {
            if (cb.getReturnValue() == 70 && !CustomFov.INSTANCE.getItemFov()) return;
            else if (CustomFov.INSTANCE.getItemFov() && cb.getReturnValue() == 70) {
                cb.setReturnValue(CustomFov.INSTANCE.getItemFovModifier());
                return;
            }
            cb.setReturnValue(CustomFov.INSTANCE.getFov());
        }
    }
}
