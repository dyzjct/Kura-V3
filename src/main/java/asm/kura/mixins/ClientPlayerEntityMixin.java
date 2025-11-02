package asm.kura.mixins;

import com.mojang.authlib.GameProfile;
import dev.kura.net.event.impl.*;
import dev.kura.net.manager.impl.EventAccessManager;
import dev.m7thh4ck.net.mod.module.impl.movement.NoSlow;
import dev.m7thh4ck.net.mod.module.impl.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class, priority = 800)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Unique
    public PlayerMotionEvent motionEvent;

    @Unique
    private boolean updateLock = false;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract float getPitch(float tickDelta);

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHook(CallbackInfo info) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            PlayerUpdateEvent event = new PlayerUpdateEvent();
            event.post();
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
        if (Velocity.INSTANCE.isEnabled() && Velocity.INSTANCE.getBlockPush()) {
            info.cancel();
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    private boolean tickMovementHook(ClientPlayerEntity player) {
        if (NoSlow.INSTANCE.isEnabled())
            return false;
        return player.isUsingItem();
    }

    @Shadow
    protected abstract void sendMovementPackets();

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void PostUpdateHook(CallbackInfo info) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            if (updateLock) return;
            PostPlayerUpdateEvent event = new PostPlayerUpdateEvent();
            event.post();
            if (event.isCancelled()) {
                info.cancel();
                if (event.getIterations() > 0) {
                    for (int i = 0; i < event.getIterations(); i++) {
                        updateLock = true;
                        tick();
                        updateLock = false;
                        sendMovementPackets();
                    }
                }
            }
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onTickMovementHead(CallbackInfo callbackInfo) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            motionEvent = new PlayerMotionEvent(PlayerMotionEvent.StageType.START, this.getX(), getY(), this.getZ(), this.getYaw(), this.getPitch(), this.isOnGround());
            motionEvent.post();
            EventAccessManager.INSTANCE.setData(motionEvent);
            if (motionEvent.isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getX()D"))
    private double posXHook(ClientPlayerEntity instance) {
        return motionEvent.getX();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getY()D"))
    private double posYHook(ClientPlayerEntity instance) {
        return motionEvent.getY();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getZ()D"))
    private double posZHook(ClientPlayerEntity instance) {
        return motionEvent.getZ();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float yawHook(ClientPlayerEntity instance) {
        return motionEvent.getYaw();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    private float pitchHook(ClientPlayerEntity instance) {
        return motionEvent.getPitch();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isOnGround()Z"))
    private boolean groundHook(ClientPlayerEntity instance) {
        return motionEvent.isOnGround();
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
    private void sendMovementPackets_Return(CallbackInfo callbackInfo) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            PlayerMotionEvent oldEvent = new PlayerMotionEvent(PlayerMotionEvent.StageType.END, motionEvent);
            oldEvent.post();
            EventAccessManager.INSTANCE.setData(oldEvent);
            if (oldEvent.isCancelled()) callbackInfo.cancel();
        }
    }

    @Inject(method = {"sendMovementPackets"}, at = {@At(value = "HEAD")})
    private void preMotion(CallbackInfo info) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            UpdateWalkingEvent.Pre event = new UpdateWalkingEvent.Pre();
            event.post();
        }
    }

    @Inject(method = {"sendMovementPackets"}, at = {@At(value = "RETURN")})
    private void postMotion(CallbackInfo info) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            UpdateWalkingEvent.Post event = new UpdateWalkingEvent.Post();
            event.post();
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            PlayerMoveEvent event = new PlayerMoveEvent(movementType, movement);
            event.post();
            if (event.isCancelled()) {
                super.move(movementType, event.getMovement());
                ci.cancel();
            }
        }
    }
}