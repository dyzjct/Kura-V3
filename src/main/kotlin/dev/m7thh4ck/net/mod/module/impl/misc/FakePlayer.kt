package dev.m7thh4ck.net.mod.module.impl.misc

import com.mojang.authlib.GameProfile
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import java.util.*

object FakePlayer : Module("FakePlayer", Category.Misc, true) {

    val displayName by setting("DisplayName", "dyzjct")
    private val syncInventory by setting("SyncInv", true)
    private val health by setting("HealthScale", 20f, 1f..36f, 0.1f)

    private var otherClientPlayerEntity: OtherClientPlayerEntity? = null

    override fun getHudInfo(): String = displayName

    init {
        onEnable {
            otherClientPlayerEntity = OtherClientPlayerEntity(
                world,
                GameProfile(UUID.fromString("04561d08-83d0-40a7-8f5b-056c3eccaa73"), displayName)
            )
            otherClientPlayerEntity!!.copyPositionAndRotation(mc.player)
            if (syncInventory) {
                otherClientPlayerEntity!!.inventory.clone(mc.player!!.inventory)
            }
            otherClientPlayerEntity!!.health = health
            otherClientPlayerEntity!!.abilities.creativeMode = false
            world.addPlayer(114514191, otherClientPlayerEntity)
        }

        onDisable {
            otherClientPlayerEntity?.let {
                it.kill()
                it.setRemoved(Entity.RemovalReason.KILLED)
                it.onRemoved()
            }

            otherClientPlayerEntity = null
        }
    }

}