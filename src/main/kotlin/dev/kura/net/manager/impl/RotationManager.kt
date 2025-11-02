package dev.kura.net.manager.impl

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.PacketEvent
import dev.kura.net.event.impl.PlayerMotionEvent
import dev.m7thh4ck.net.util.math.vector.VectorUtils.toVec3d
import dev.m7thh4ck.net.util.player.RotationUtil
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CopyOnWriteArrayList

object RotationManager {

    private val rotateList = CopyOnWriteArrayList<RotateInfo>()
    private var spoofMap = CopyOnWriteArrayList<RotateInfo>()
    private var doRotation = true

    fun init() {
        rotateList.clear()
    }

    init {

        eventListener<PlayerMotionEvent>(Int.MAX_VALUE, true) {
            rotateList.sortBy { it.priority }

            val first = rotateList.firstOrNull() ?: return@eventListener

            if (!doRotation) return@eventListener

            it.setRotation(first.yaw, first.pitch)

            rotateList.removeFirst()
        }

        eventListener<PacketEvent.Send>(Int.MAX_VALUE) { event ->
            if (!doRotation) return@eventListener
            spoofMap.sortBy { it.priority }
            spoofMap.forEach {
                when (event.packet) {
                    is PlayerMoveC2SPacket -> {
                        val packet = it
                        event.packet.yaw = packet.yaw
                        event.packet.pitch = packet.pitch
                        spoofMap.remove(packet)
                        spoofMap.removeFirst()
                    }
                }
            }
        }

    }

    fun addRotation(yaw: Float, pitch: Float, priority: Int = 10) {
        rotateList.add(RotateInfo(yaw, pitch, priority))
    }

    fun addRotation(vec3d: Vec3d, priority: Int = 10) {
        val angle = RotationUtil.calculateAngle(vec3d)
        rotateList.add(RotateInfo(angle[0], angle[1], priority))
    }

    fun addRotation(pos: BlockPos, priority: Int = 10) {
        val angle = RotationUtil.calculateAngle(pos.toVec3d())
        rotateList.add(RotateInfo(angle[0], angle[1], priority))
    }

    fun startRotation() {
        doRotation = true
    }

    fun stopRotation() {
        doRotation = false
    }


    fun clearList() {
        rotateList.clear()
    }

    data class RotateInfo(val yaw: Float, val pitch: Float, val priority: Int = 10)
}