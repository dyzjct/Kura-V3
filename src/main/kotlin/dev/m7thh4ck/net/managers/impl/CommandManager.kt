package dev.m7thh4ck.net.managers.impl

import dev.m7thh4ck.net.command.Command
import dev.m7thh4ck.net.command.impl.*
import dev.m7thh4ck.net.command.impl.*

object CommandManager {
    private val commandMap = hashMapOf<Array<String>, Command>()
    private var commandPrefix = '.'

    fun init() {
        createCommand(HelpCommand())
        createCommand(ToggleCommand())
        createCommand(ModuleCommand())
        createCommand(FriendCommand())
        createCommand(ConfigCommand())
        createCommand(PrefixCommand())
        createCommand(BindCommand())
    }

    private fun createCommand(cmd: Command) {
        commandMap[cmd.key] = cmd
    }

    fun run(message: String): Boolean {
        val filter = message.split(" ").filter { it.isNotEmpty() }.toTypedArray()
        if (filter.toList().isEmpty()) return false
        val command = filter[0].substring(1)
        commandMap.forEach { (n, u) ->
            if ( n.aliasesContains(command)) {
                val args = filter.toList().toMutableList()
                args.removeFirst()
                u.run(args.toList().toTypedArray())
                return true
            }
        }
        return false
    }

    private fun Array<String>.aliasesContains(s: String): Boolean {
        return this.find { it.equals(s, true) } != null
    }

    fun getPrefix(): Char {
        return commandPrefix
    }

    fun setPrefix(ch: Char) {
        commandPrefix = ch
    }
}