package dev.kura.net.command.impl

import dev.kura.net.command.Command
import dev.kura.net.event.impl.ModuleManager

class ToggleCommand
    : Command(arrayOf("t", "toggle")) {
    override fun run(args: Array<String>) {
        if (args.size == 1) {
            ModuleManager.getByName(args[0])?.toggle()
        }
    }
}
