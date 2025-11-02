package dev.kura.net.event.impl

import dev.kura.net.event.Event
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

sealed class WorldEvent : Event() {
    internal object Unload : WorldEvent()
    internal object Load : WorldEvent()

    sealed class Entity(val entity: net.minecraft.entity.Entity) : WorldEvent() {
        class Add(entity: net.minecraft.entity.Entity) : Entity(entity)

        class Remove(entity: net.minecraft.entity.Entity) : Entity(entity)
    }

    class ServerBlockUpdate(
        val pos: BlockPos,
        val oldState: BlockState,
        val newState: BlockState
    ) : WorldEvent()

    class ClientBlockUpdate(
        val pos: BlockPos,
        val oldState: BlockState,
        val newState: BlockState
    ) : WorldEvent()

    class RenderUpdate(
        val blockPos: BlockPos
    ) : WorldEvent()
}