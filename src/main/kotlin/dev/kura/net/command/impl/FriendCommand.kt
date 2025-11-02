package dev.kura.net.command.impl

import dev.kura.net.command.Command
import dev.kura.net.manager.impl.FriendManager
import dev.m7thh4ck.net.util.helper.ChatUtil

class FriendCommand: Command(arrayOf("friend")) {
    override fun run(args: Array<String>) {
        if (args.size != 2 && args.size != 1) {
            ChatUtil.sendRawMessage("friend [add/del] [friendName]")
            return
        }
        when (args[0]) {
            "add" -> {
                FriendManager.friends.forEach {
                    if (it == args[1]) {
                        ChatUtil.sendRawMessage("Failed to add ${args[1]}")
                        return
                    }
                }
                FriendManager.friends.add(args[1])
                ChatUtil.sendRawMessage("Added friend ${args[1]}")
            }
            "del" -> {
                if (FriendManager.friends.remove(args[1])) ChatUtil.sendRawMessage("Removed friend ${args[1]}")
                else ChatUtil.sendRawMessage("Failed to remove ${args[1]}")
            }
            "list" -> {
                ChatUtil.sendRawMessage("Friends: ${FriendManager.friends}")
            }
            else -> {
                ChatUtil.sendRawMessage("friend [add/del] [friendName]")
            }
        }
    }


}