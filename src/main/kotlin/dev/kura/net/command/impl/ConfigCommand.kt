package dev.kura.net.command.impl

import dev.kura.net.command.Command
import dev.kura.net.manager.impl.ConfigManager
import dev.kura.net.utils.helper.ChatUtil

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