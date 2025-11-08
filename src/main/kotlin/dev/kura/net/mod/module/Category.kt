package dev.kura.net.mod.module

import dev.kura.net.utils.interfaces.DisplayEnum

enum class Category(override val displayName: CharSequence): DisplayEnum {

    Combat("Combat"),
    Misc("Misc"),
    Movement("Movement"),
    Render("Render"),
    Player("Player"),
    Client("Client");

    override fun toString(): String = displayString

}