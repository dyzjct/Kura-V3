package dev.kura.net.event.impl

import dev.kura.net.event.Event

sealed class GameLoopEvent: Event(){
    data object Start : GameLoopEvent()
    data object Tick : GameLoopEvent()
    data object Render : GameLoopEvent()
    data object End : GameLoopEvent()
}