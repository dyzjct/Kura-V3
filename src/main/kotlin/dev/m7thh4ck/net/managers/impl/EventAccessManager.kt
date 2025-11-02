package dev.m7thh4ck.net.managers.impl

import dev.kura.net.event.impl.PlayerMotionEvent
object EventAccessManager {
    private var playerMotion: PlayerMotionEvent? = null

    fun getData(): PlayerMotionEvent? {
        if (playerMotion != null) {
            return playerMotion
        }
        return null
    }

    fun setData(e: PlayerMotionEvent) {
        playerMotion = e
    }
}