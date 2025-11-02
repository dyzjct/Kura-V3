package dev.m7thh4ck.net.mod.module.impl.combat

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.StopUsingItemEvent
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import net.minecraft.entity.Entity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround
import net.minecraft.util.math.BlockPos
import kotlin.math.floor
import kotlin.math.roundToInt


object BowBomb: Module("BowBomb", Category.Combat) {
    private var NewBow by setting("NewBow", false)
    private var OldBow by setting("OldBow", false)
    private val amountProperty by setting("Amount", 100, 10..5000, 1)

    init {
        eventListener<StopUsingItemEvent> {
            val player = mc.player ?: return@eventListener
            if (player.mainHandStack.item == Items.BOW) {
                player.networkHandler.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING))
                if(OldBow) {
                    for (i in 0 until amountProperty) {
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                player.x,
                                player.y - 1.0E-9,
                                player.z,
                                true
                            )
                        )
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                player.x,
                                player.y + 1.0E-9,
                                player.z,
                                false
                            )
                        )
                    }
                }
                if(NewBow){
                    for (i in 0 until amountProperty) {
                        val pos: BlockPos = getFlooredPosition(player as Entity).add(0, 0, 0)
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                pos.x+ 0.0,
                                pos.y - 1.0E-9,
                                pos.z+ 0.0,
                                false
                            )
                        )
                        player.networkHandler.sendPacket(
                            PositionAndOnGround(
                                pos.x- 0.0,
                                pos.y + 1.0E-9,
                                pos.z- 0.0,
                                false
                            )
                        )
                    }
                }


            }
        }
    }
    private fun getFlooredPosition(entity: Entity): BlockPos {
        return BlockPos(
            floor(entity.x).toInt(),
            entity.y.roundToInt().toDouble().toInt(),
            floor(entity.z).toInt()
        )
    }
}