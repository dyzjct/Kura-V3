package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class BlockEvent(val pos: BlockPos, val facing: Direction): Event()