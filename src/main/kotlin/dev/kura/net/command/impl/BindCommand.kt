package dev.kura.net.command.impl

import dev.kura.net.command.Command
import dev.kura.net.event.impl.ModuleManager
import dev.m7thh4ck.net.util.helper.ChatUtil
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Formatting
import java.util.*


class BindCommand : Command(arrayOf("bind")) {
    override fun run(args: Array<String>) {
        when (args.size) {
            2 -> {
                ModuleManager.getByName(args[0])?.let {

                    val stringKey = args[1]

                    val key = if (stringKey == "None" || stringKey == "none") -1
                    else {
                        try {
                            InputUtil.fromTranslationKey("key.keyboard." + stringKey.lowercase(Locale.getDefault())).code
                        } catch (e: NumberFormatException) {
                            ChatUtil.sendRawMessage("There is no such button")
                            return
                        }
                    }

                    if (key == 0) {
                        ChatUtil.sendRawMessage("Unknown key '$stringKey'!")
                        return
                    }

                    it.keybind.key = key
                    ChatUtil.sendRawMessage(
                        "Bind for " + Formatting.GREEN + it.name +
                                Formatting.WHITE + " set to " + Formatting.GRAY + stringKey.uppercase(Locale.getDefault())
                    )

                }
            }

            else -> {
                ChatUtil.sendRawMessage("bind [moduleName] [key]")
            }
        }
    }
}