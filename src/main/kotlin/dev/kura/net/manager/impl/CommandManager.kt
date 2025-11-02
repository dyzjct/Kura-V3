package dev.kura.net.manager.impl

import dev.kura.net.command.Command
import dev.kura.net.command.impl.BindCommand
import dev.kura.net.command.impl.ConfigCommand
import dev.kura.net.command.impl.FriendCommand
import dev.kura.net.command.impl.HelpCommand
import dev.kura.net.command.impl.ModuleCommand
import dev.kura.net.command.impl.PrefixCommand
import dev.kura.net.command.impl.ToggleCommand

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