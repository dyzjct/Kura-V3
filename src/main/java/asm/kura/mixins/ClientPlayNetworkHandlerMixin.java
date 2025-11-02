package asm.kura.mixins;

import dev.kura.net.KURA;
import dev.m7thh4ck.net.managers.impl.CommandManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (message.charAt(0) == CommandManager.INSTANCE.getPrefix()) {
            KURA.LOGGER.info("command");
            CommandManager.INSTANCE.run(message);
            ci.cancel();
        }
    }
}
