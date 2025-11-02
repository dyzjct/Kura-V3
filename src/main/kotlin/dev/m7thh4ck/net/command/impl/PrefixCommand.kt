package dev.m7thh4ck.net.command.impl

import dev.m7thh4ck.net.command.Command
import dev.m7thh4ck.net.managers.impl.CommandManager
import dev.m7thh4ck.net.util.helper.ChatUtil

class PrefixCommand: Command(arrayOf("prefix")) {
    override fun run(args: Array<String>) {
        if (args.size != 1) {
            ChatUtil.sendRawMessage("prefix [prefix]")
            return
        }
        if (args[0].length != 1) {
            ChatUtil.sendRawMessage("only one ch")
        }

        CommandManager.setPrefix(args[0][0])
        ChatUtil.sendRawMessage("prefix set to ${args[0]}")
    }


}