package dev.m7thh4ck.asm.mixin;

import dev.m7thh4ck.net.event.impl.KeyBoardEvent;
import dev.m7thh4ck.net.mod.gui.screen.ClickGUIScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null){
            if ((MinecraftClient.getInstance().currentScreen == null ||
                    MinecraftClient.getInstance().currentScreen == ClickGUIScreen.INSTANCE) && action == GLFW.GLFW_PRESS) {
                if (key != GLFW.GLFW_KEY_UNKNOWN) {
                    KeyBoardEvent event = new KeyBoardEvent(key);
                    event.post();
                }
            }
        }
    }
}
