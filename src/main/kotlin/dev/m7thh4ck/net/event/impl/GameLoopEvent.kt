package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event

sealed class GameLoopEvent: Event(){
    data object Start : GameLoopEvent()
    data object Tick : GameLoopEvent()
    data object Render : GameLoopEvent()
    data object End : GameLoopEvent()
}