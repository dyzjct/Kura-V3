package dev.m7thh4ck.net.event.impl

import dev.kura.net.asmimpl.IVec3d
import dev.m7thh4ck.net.event.CancellableEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.MovementType
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

class PlayerMoveEvent(var type: MovementType, var movement: Vec3d) : CancellableEvent() {
    fun setSpeed(speed: Double) {
        val player = MinecraftClient.getInstance().player ?: return
        var yaw = player.yaw
        player.input?.let {
            var forward = it.movementForward.toDouble()
            var strafe = it.movementSideways.toDouble()
            if (forward == 0.0 && strafe == 0.0) {
                (movement as IVec3d).setXZ(0.0,0.0)
            } else {
                if (forward != 0.0) {
                    if (strafe > 0) {
                        yaw += (if (forward > 0) -45 else 45).toFloat()
                    } else if (strafe < 0) {
                        yaw += (if (forward > 0) 45 else -45).toFloat()
                    }
                    strafe = 0.0
                    forward = if (forward > 0) {
                        1.0
                    } else {
                        -1.0
                    }
                }
                val cos = cos(Math.toRadians((yaw + 90).toDouble()))
                val sin = sin(Math.toRadians((yaw + 90).toDouble()))
                (movement as IVec3d).setXZ(
                    (forward * speed * cos + strafe * speed * sin),
                    (forward * speed * sin - strafe * speed * cos)
                )
            }
        }
    }
}