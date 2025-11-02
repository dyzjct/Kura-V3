package dev.m7thh4ck.net.mod.module

import dev.m7thh4ck.net.util.interfaces.DisplayEnum

enum class Category(override val displayName: CharSequence): DisplayEnum {

    Combat("Combat"),
    Misc("Misc"),
    Movement("Movement"),
    Render("Render"),
    Player("Player"),
    Client("Client");

    override fun toString(): String = displayString

}