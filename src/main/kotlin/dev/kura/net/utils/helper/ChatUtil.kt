package dev.kura.net.utils.helper

import dev.kura.net.utils.Util
import net.minecraft.text.Text
import net.minecraft.util.Formatting


object ChatUtil : Util() {
    const val DeleteID = 94423
    var SECTIONSIGN = "\u00A7"
    var BLACK = SECTIONSIGN + "0"
    var DARK_BLUE = SECTIONSIGN + "1"
    var DARK_GREEN = SECTIONSIGN + "2"
    var DARK_AQUA = SECTIONSIGN + "3"
    var DARK_RED = SECTIONSIGN + "4"
    var DARK_PURPLE = SECTIONSIGN + "5"
    var GOLD = SECTIONSIGN + "6"
    var GRAY = SECTIONSIGN + "7"
    var DARK_GRAY = SECTIONSIGN + "8"
    var BLUE = SECTIONSIGN + "9"
    var GREEN = SECTIONSIGN + "a"
    var AQUA = SECTIONSIGN + "b"
    var RED = SECTIONSIGN + "c"
    var LIGHT_PURPLE = SECTIONSIGN + "d"
    var YELLOW = SECTIONSIGN + "e"
    var WHITE = SECTIONSIGN + "f"
    var OBFUSCATED = SECTIONSIGN + "k"
    var BOLD = SECTIONSIGN + "l"
    var STRIKE_THROUGH = SECTIONSIGN + "m"
    var UNDER_LINE = SECTIONSIGN + "n"
    var ITALIC = SECTIONSIGN + "o"
    var RESET = SECTIONSIGN + "r"
    var colorMSG = SECTIONSIGN + "r"
    var colorKANJI = SECTIONSIGN + "d"
    var colorWarn = SECTIONSIGN + "6" + SECTIONSIGN + "l"
    var colorError = SECTIONSIGN + "4" + SECTIONSIGN + "l"
    var colorBracket = SECTIONSIGN + "7"
    var msgCount = 0
    var tempMsg: String? = null

    @JvmStatic
    fun sendRawMessage(message: CharSequence) {
        mc.inGameHud.chatHud.addMessage(Text.of("${Formatting.AQUA}[Kura] ${Formatting.WHITE}$message"))
    }

}