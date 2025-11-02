package asm.kura.mixins;

import dev.m7thh4ck.net.event.impl.BlockEvent;
import dev.m7thh4ck.net.event.impl.StopUsingItemEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "stopUsingItem", at = @At("HEAD"), cancellable = true)
    private void stopUsingItem(PlayerEntity player, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
            StopUsingItemEvent event = new StopUsingItemEvent();
            event.post();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = {"attackBlock"}, at = @At("HEAD"))
    private void onPlayerClickBlock(final BlockPos pos, final Direction face, final CallbackInfoReturnable<Boolean> info) {
        new BlockEvent(pos, face).post();
    }
}
