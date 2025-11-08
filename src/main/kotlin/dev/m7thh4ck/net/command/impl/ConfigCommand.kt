package dev.m7thh4ck.net.command.impl

import dev.m7thh4ck.net.command.Command
import dev.m7thh4ck.net.managers.impl.ConfigManager
import dev.m7thh4ck.net.util.helper.ChatUtil

class ConfigCommand: Command(arrayOf("config")) {
    override fun run(args: Array<String>) {
        if (args.size != 1) {
            ChatUtil.sendRawMessage("config [save]")
            return
        }

        when (args[0]) {
            "save" -> {
                ConfigManager.saveAll()
                ChatUtil.sendRawMessage("Config saved.")
            }
        }
    }

}