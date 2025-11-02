package dev.m7thh4ck.asm.mixin;

import dev.m7thh4ck.net.event.impl.PlayerMoveEvent;
import dev.m7thh4ck.net.mod.module.impl.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public class EntityMixin {

    @ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void pushAwayFromHook(Args args) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            double value = 1;
            if (Velocity.INSTANCE.isEnabled() && Velocity.INSTANCE.getEntityPush()) {
                value = 0;
            }
            args.set(0, (double) args.get(0) * value);
            args.set(1, (double) args.get(1) * value);
            args.set(2, (double) args.get(2) * value);
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            if ((Object) this == MinecraftClient.getInstance().player) {
                PlayerMoveEvent event = new PlayerMoveEvent(movementType, movement);
                event.post();
            }
        }
    }
}
