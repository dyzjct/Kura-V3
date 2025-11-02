package dev.m7thh4ck.net.managers.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.kura.net.KURA
import dev.m7thh4ck.net.settings.*
import dev.m7thh4ck.net.util.keyboard.Bind
import dev.m7thh4ck.net.mod.module.Module
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object ConfigManager {

    private val path = File(KURA.NAME)

    fun init() {
        initModules()
        initFriend()
        initCommand()
    }

    fun unload() {
        saveAll()
    }

    private fun initModules() {
        if (!path.exists()) path.mkdirs()
        for (mod in ModuleManager.getModules()) {
            val modPath = getModulePath(mod)
            if (!modPath.exists()) {
                saveModuleConfig(mod)
            } else {
                loadModule(mod)
            }
        }
    }

    private fun initFriend() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val friendFile = File(path, "Friends.json")
        if (!friendFile.exists()) {
            friendFile.parentFile.mkdirs()
            friendFile.createNewFile()
        } else {
            val friendJson = Gson().fromJson(
                String(Files.readAllBytes(friendFile.toPath()), StandardCharsets.UTF_8),
                ArrayList::class.java
            ) ?: return
            FriendManager.friends.clear()
            friendJson.forEach {
                FriendManager.friends.add(it.toString())
            }
        }
    }

    private fun initCommand() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val commandFile = File(path, "Prefix.txt")
        if (!commandFile.exists()) {
            commandFile.parentFile.mkdirs()
            commandFile.createNewFile()
            return
        }
        val prefix = commandFile.readText()
        if (prefix.isEmpty()) return
        CommandManager.setPrefix(prefix[0])
    }

    private fun saveCommand() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val commandFile = File(path, "Prefix.txt")
        if (!commandFile.exists()) {
            commandFile.parentFile.mkdirs()
            commandFile.createNewFile()
        }
        commandFile.writeText(CommandManager.getPrefix().toString())
    }

    private fun saveFriends() {
        if (!path.exists()) {
            path.mkdirs()
        }
        val friendFile = File(path, "Friends.json")
        if (!friendFile.exists()) {
            friendFile.parentFile.mkdirs()
            friendFile.createNewFile()
        }
        Files.write(
            friendFile.toPath(), Gson().toJson(FriendManager.friends).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    private fun loadFriends() {
        val friendFile = File(path, "Friends.json")
        if (!friendFile.exists()) {
            return
        }
        val friendJson = Gson().fromJson(
            String(Files.readAllBytes(friendFile.toPath()), StandardCharsets.UTF_8),
            ArrayList::class.java
        ) ?: return
        FriendManager.friends.clear()
        friendJson.forEach {
            FriendManager.friends.add(it.toString())
        }
    }

    private fun saveModuleConfig(mod: Module) {
        val modPath = getModulePath(mod)
        if (!modPath.exists()) {
            modPath.parentFile.mkdirs()
            modPath.createNewFile()
        }
        modPath.parentFile.mkdirs()
        modPath.createNewFile()
        val moduleJson = JsonObject()
        moduleJson.addProperty("Name", mod.name)
        moduleJson.addProperty("Toggle", mod.isEnabled())
        if (mod.getSettings().isNotEmpty()) {
            val settingObject = JsonObject()
            for (setting in mod.getSettings()) {
                saveSetting(setting, settingObject)
            }
            moduleJson.add("Settings", settingObject)
        }
        Files.write(
            modPath.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(moduleJson).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    private fun loadModule(mod: Module) {
        val modPath = getModulePath(mod)
        if (!modPath.exists()) return
        val moduleJson = Gson().fromJson(
            String(Files.readAllBytes(modPath.toPath()), StandardCharsets.UTF_8),
            JsonObject::class.java
        ) ?: return
        try {
            mod.name = moduleJson.get("Name").asString
            val toggle = moduleJson.get("Toggle").asBoolean
            mod.setEnableSilent(toggle)
        } catch (e: NullPointerException) {
            KURA.LOGGER.error("Loading config failed! Please delete \"M7thH4ck\" folder and retry")
        }

        val element = moduleJson.get("Settings") ?: return
        val settingsJson = element.asJsonObject
        if (settingsJson != null) {
            for (setting in mod.getSettings()) {
                setSetting(setting, settingsJson)
            }
        }
    }

    private fun saveAllModules() {
        for (mod in ModuleManager.getModules()) {
            saveModuleConfig(mod)
        }
    }


    private fun loadAllModules() {
        for (mod in ModuleManager.getModules()) {
            loadModule(mod)
        }
    }

    private fun saveSetting(setting: AbstractSetting<*>, jsonObject: JsonObject): JsonObject {
        when (setting) {
            is BooleanSetting -> jsonObject.addProperty(setting.displayName.toString(), setting.value)
            is KeyBindSetting -> jsonObject.addProperty(setting.displayName.toString(), setting.value.key)
            is FloatSetting -> jsonObject.addProperty(setting.displayName.toString(), setting.value)
            is DoubleSetting -> jsonObject.addProperty(setting.displayName.toString(), setting.value)
            is IntSetting -> jsonObject.addProperty(setting.displayName.toString(), setting.value)
            is EnumSetting<*> -> jsonObject.addProperty(setting.displayName.toString(), setting.value.name)
            is StringSetting -> jsonObject.addProperty(setting.displayName.toString(), setting.value)
        }
        return jsonObject
    }

    private fun setSetting(setting: AbstractSetting<*>, jsonObject: JsonObject) {
        if (jsonObject.has(setting.displayName.toString())) {
            when (setting) {
                is BooleanSetting -> setting.value = jsonObject.get(setting.displayName.toString()).asBoolean
                is KeyBindSetting -> setting.value = Bind(jsonObject.get(setting.displayName.toString()).asInt)
                is FloatSetting -> setting.value = jsonObject.get(setting.displayName.toString()).asFloat
                is DoubleSetting -> setting.value = jsonObject.get(setting.displayName.toString()).asDouble
                is IntSetting -> setting.value = jsonObject.get(setting.displayName.toString()).asInt
                is StringSetting -> setting.value = jsonObject.get(setting.displayName.toString()).asString
                is EnumSetting<*> -> setting.setValueByString(jsonObject.get(setting.displayName.toString()).asString)
            }
        }
    }

    private fun getModulePath(module: Module): File {
        return File("$path/modules/${module.category.name}/${module.name}.json")
    }

    fun saveAll() {
        saveAllModules()
        saveFriends()
        saveCommand()
    }

    fun loadAll() {
        loadAllModules()
        loadFriends()
    }

}