package dev.kura.net.event.impl

import dev.kura.net.event.Event
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class BlockEvent(val pos: BlockPos, val facing: Direction): Event()