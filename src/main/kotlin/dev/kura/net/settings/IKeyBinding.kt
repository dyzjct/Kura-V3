package dev.kura.net.settings


interface IKeyBinding {
    /**
     * @return true if the user is actually pressing this key on their keyboard.
     */
    val isActallyPressed: Boolean

    /**
     * Resets the pressed state to whether or not the user is actually pressing
     * this key on their keyboard.
     */
    fun resetPressedState()
}