package dev.m7thh4ck.net.settings

import java.util.*


class ModeSetting(name: String?, var mode: String, vararg modes: String?) : Setting(name) {
    var modes: List<String>
    private var index: Int

    init {
        this.modes = Arrays.asList(*modes) as List<String>
        index = this.modes.indexOf(mode)
    }

    var selected: String
        get() = mode
        set(selected) {
            mode = selected
            index = modes.indexOf(selected)
        }

    fun `is`(mode: String): Boolean {
        return mode == mode
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
        mode = modes[index]
    }

    fun cycle() {
        if (index < modes.size - 1) {
            index++
            mode = modes[index]
        } else if (index >= modes.size - 1) {
            index = 0
            mode = modes[0]
        }
    }

    fun getMode() {

    }
}