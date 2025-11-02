package dev.kura.net.mod.module.impl.render

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.GameLoopEvent
import dev.kura.net.event.impl.Render3DEvent
import dev.kura.net.utils.entity.EntityUtil.aroundBlock
import dev.kura.net.utils.graphics.Render3DEngine
import dev.kura.net.utils.world.BlockUtil.blockType
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import java.awt.Color
import kotlin.math.roundToInt

object HoleESP : Module("HoleESP", Category.Render) {

    private val range by setting("Range", 6.0f, 0.5f..20.0f, 0.1f)

    private val obsRed by setting("ObsRed", 255, 0..255, 1)
    private val obsGreen by setting("ObsGreen", 70, 0..255, 1)
    private val obsBlue by setting("ObsBlue", 70, 0..255, 1)
    private val obsAlpha by setting("ObsAlpha", 100, 0..255, 1)

    private val brRed by setting("BrRed", 70, 0..255, 1)
    private val brGreen by setting("BrGreen", 255, 0..255, 1)
    private val brBlue by setting("BrBlue", 255, 0..255, 1)
    private val brAlpha by setting("BrAlpha", 100, 0..255, 1)

    private val outline by setting("Outline", 2.0f, 0.0f..20.0f, 0.5f)
    private val outlineAlpha by setting("OutlineAlpha", 255, 0..255, 1)

    private val brHoles = mutableListOf<BlockPos>()
    private val obsHoles = mutableListOf<BlockPos>()

    init {

        eventListener<Render3DEvent> {
            if (fullNullCheck()) return@eventListener

            brHoles.forEach {
                Render3DEngine.drawHoleOutline(Box(it), Color(brRed, brGreen, brBlue, outlineAlpha), outline)
                Render3DEngine.drawFilledBox(Box(it), Color(brRed, brGreen, brBlue, brAlpha))
            }
            obsHoles.forEach {
                Render3DEngine.drawHoleOutline(Box(it), Color(obsRed, obsGreen, obsBlue, outlineAlpha), outline)
                Render3DEngine.drawFilledBox(Box(it), Color(obsRed, obsGreen, obsBlue, obsAlpha))
            }
        }

        eventListener<GameLoopEvent.Tick> {
            if (fullNullCheck()) return@eventListener

            brHoles.clear()
            obsHoles.clear()

            player.blockPos.aroundBlock(range.roundToInt())
                .forEach {
                    if (isHole(it)) {
                        if (isBrHole(it)) {
                            brHoles.add(it)
                        } else {
                            obsHoles.add(it)
                        }
                    }
                }
        }

    }

    private fun isBrHole(pos: BlockPos): Boolean {
        for (dir in Direction.entries) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue
            if (pos.offset(dir).blockType() != Blocks.BEDROCK) return false
        }
        return true
    }

    private fun isHole(pos: BlockPos): Boolean {
        if (pos.blockType() != Blocks.AIR) return false
        for (dir in Direction.entries) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue
            if (pos.offset(dir).blockType() != Blocks.BEDROCK &&
                pos.offset(dir).blockType() != Blocks.OBSIDIAN
            ) return false
        }
        return true
    }

}