package dev.m7thh4ck.net.util.keyboard

import org.lwjgl.glfw.GLFW
import java.util.*


class Bind(var key: Int) {

    val bind: String
        get() {
            var kn = if (key > 0) GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key)) else "None"
            if (kn == null) {
                try {
                    for (declaredField in GLFW::class.java.getDeclaredFields()) {
                        if (declaredField.name.startsWith("GLFW_KEY_")) {
                            val a = declaredField[null] as Int
                            if (a == key) {
                                val nb = declaredField.name.substring("GLFW_KEY_".length)
                                kn = nb.substring(0, 1).uppercase(Locale.getDefault()) + nb.substring(1)
                                    .lowercase(Locale.getDefault())
                            }
                        }
                    }
                } catch (ignored: Exception) {
                    kn = "unknown." + key
                }
            }
            return if (key == -1) "None" else (kn + "").uppercase(Locale.getDefault())
        }
}