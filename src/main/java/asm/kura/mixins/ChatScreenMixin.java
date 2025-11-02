package asm.kura.mixins;

import dev.m7thh4ck.net.event.impl.MessageSentEvent;
import dev.m7thh4ck.net.managers.impl.CommandManager;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    public String normalize(String message) {
        return StringHelper.truncateChat(StringUtils.normalizeSpace(message.trim()));
    }

    /**
     * @author dyzjct
     * @reason FUCK U MOJANG
     */

    @Overwrite
    public boolean sendMessage(String message, boolean addToHistory) {
        if ((message = this.normalize(message)).isEmpty()) {
            return true;
        }
        if (client != null) {
            if (message.startsWith("/")) {
                this.client.player.networkHandler.sendChatCommand(message.substring(1));
                this.client.inGameHud.getChatHud().addToMessageHistory(message);
                return true;
            }
            if (message.startsWith(String.valueOf(CommandManager.INSTANCE.getPrefix()))) {
                this.client.inGameHud.getChatHud().addToMessageHistory(message);
                CommandManager.INSTANCE.run(message);
                return true;
            }
        }
        MessageSentEvent messageSentEvent = new MessageSentEvent(message);
        messageSentEvent.post();
        if (client != null) {
            if (addToHistory && client.inGameHud != null && client.inGameHud.getChatHud() != null) {
                this.client.inGameHud.getChatHud().addToMessageHistory(message);
            }
            if (client.player != null && client.getNetworkHandler() != null) {
                this.client.player.networkHandler.sendChatMessage(messageSentEvent.getMessage());
                return !messageSentEvent.isCancelled();
            }
        }
        return true;
    }
}
