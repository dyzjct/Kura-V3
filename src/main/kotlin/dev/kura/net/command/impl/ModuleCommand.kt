package dev.kura.net.command.impl

import dev.kura.net.command.Command
import dev.kura.net.event.impl.ModuleManager
import dev.kura.net.settings.BooleanSetting
import dev.kura.net.settings.IntSetting
import dev.kura.net.settings.StringSetting
import dev.kura.net.utils.helper.ChatUtil

class ModuleCommand : Command(arrayOf("module")) {
    override fun run(args: Array<String>) {
        when (args.size) {
            1 -> {  // 查询所有选项
                val mod = ModuleManager.getByName(args[0])
                if (mod == null) {
                    ChatUtil.sendRawMessage("Can't find module \"${args[0]}\"")
                    return
                }
                ChatUtil.sendRawMessage("Module: ${args[0]}")
                for (setting in mod.getSettings()) {
                    ChatUtil.sendRawMessage("${setting.displayName} = ${setting.value.toString()}")
                }
            }

            3 -> {  // 查询选项 1: moduleName 2: get 3: settingName
                if (args[1] == "get") {
                    val mod = ModuleManager.getByName(args[0])
                    if (mod == null) {
                        ChatUtil.sendRawMessage("Can't find module \"${args[0]}\"")
                        return
                    }
                    for (setting in mod.getSettings()) {
                        if (setting.displayName == args[2])
                            ChatUtil.sendRawMessage("${args[0]}.${args[2]} = ${setting.value.toString()}")
                    }
                } else {
                    ChatUtil.sendRawMessage("module [moduleName] ( [set/get] [settingName] ([value]) )")
                }
            }

            4 -> {  // 设置选项 1: moduleName 2: set 3: settingName 4: value
                if (args[1] == "set") {
                    val mod = ModuleManager.getByName(args[0])
                    if (mod == null) {
                        ChatUtil.sendRawMessage("Can't find module \"${args[0]}\"")
                        return
                    }
                    for (setting in mod.getSettings()) {
                        if (setting.displayName == args[2]) {
                            when (setting) {
                                is IntSetting -> {
                                    setting.value = args[3].toInt()
                                }

                                is BooleanSetting -> {
                                    when (args[3]) {
                                        "true" -> setting.value = true
                                        "false" -> setting.value = false
                                        else -> ChatUtil.sendRawMessage("${args[3]} is not a boolean value! pls use \"true\" or \"false\"")
                                    }
                                }

                                is StringSetting -> {
                                    setting.value = args[3]
                                }

                                else -> {
                                    ChatUtil.sendRawMessage("Set value failed!")
                                }
                            }
                        }
                    }
                } else {
                    ChatUtil.sendRawMessage("module [moduleName] ( [set/get] [settingName] ([value]) )")
                }
            }

            else -> {
                ChatUtil.sendRawMessage("module [moduleName] ( [set/get] [settingName] ([value]) )")
            }
        }

    }
}